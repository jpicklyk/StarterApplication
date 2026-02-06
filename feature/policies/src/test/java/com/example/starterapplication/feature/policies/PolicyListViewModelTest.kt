package com.example.starterapplication.feature.policies

import com.github.jpicklyk.knox.licensing.domain.KnoxLicenseInitializer
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.sfelabs.knox.core.feature.api.PolicyCategory
import net.sfelabs.knox.core.feature.api.PolicyComponent
import net.sfelabs.knox.core.feature.api.PolicyGroup
import net.sfelabs.knox.core.feature.api.PolicyGroupingStrategy
import net.sfelabs.knox.core.feature.api.PolicyState
import net.sfelabs.knox.core.feature.api.ResolvedPolicyGroup
import net.sfelabs.knox.core.feature.domain.registry.PolicyRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests demonstrating testing patterns for Knox-based ViewModels.
 *
 * ## Test Setup Requirements
 *
 * ### 1. Gradle Dependencies
 * Add these to your app/build.gradle.kts:
 * ```kotlin
 * testImplementation(projects.knoxCore.testing)        // Knox testing utilities
 * testImplementation(libs.junit)                        // JUnit 4
 * testImplementation(libs.bundles.mockk)               // MockK for mocking
 * testImplementation(libs.kotlinx.coroutines.test)     // Coroutines test utilities
 * ```
 *
 * ### 2. Coroutine Testing Setup
 * ViewModels use `viewModelScope` which runs on `Dispatchers.Main`. In unit tests,
 * `Dispatchers.Main` is not available, so we must replace it with a test dispatcher:
 *
 * ```kotlin
 * @Before
 * fun setup() {
 *     Dispatchers.setMain(testDispatcher)  // Replace Main dispatcher
 * }
 *
 * @After
 * fun tearDown() {
 *     Dispatchers.resetMain()  // Restore original dispatcher
 * }
 * ```
 *
 * ### 3. Choosing a Test Dispatcher
 * - [UnconfinedTestDispatcher]: Executes coroutines eagerly/immediately.
 *   Best for most tests where you want synchronous behavior.
 *
 * - [StandardTestDispatcher]: Requires manual advancement with `advanceUntilIdle()`.
 *   Best for testing timing, delays, or sequential coroutine behavior.
 *
 * ### 4. MockK for Dependencies
 * Use MockK to create fake implementations of interfaces:
 * ```kotlin
 * val mockRegistry = mockk<PolicyRegistry>()
 * every { mockRegistry.getAllPolicies() } returns listOf(...)
 * coEvery { mockRegistry.suspendingFunction() } returns result  // For suspend functions
 * ```
 *
 * ### 5. StateFlow Testing
 * Test StateFlow values directly after triggering actions:
 * ```kotlin
 * viewModel.doAction()
 * advanceUntilIdle()  // Wait for coroutines to complete
 * assertEquals(expected, viewModel.uiState.value)
 * ```
 *
 * ## Knox-Specific Testing Notes
 *
 * - **PolicyRegistry**: Mock this to return test policies without Knox SDK
 * - **PolicyGroupingStrategy**: Mock to control how policies are grouped
 * - **KnoxLicenseInitializer**: Mock the licenseStatus flow for license state testing
 * - **knox-core:testing module**: Provides [TestDispatcherProvider] for injecting
 *   test dispatchers into classes that use [DispatcherProvider]
 *
 * @see net.sfelabs.knox.core.testing.coroutines.TestDispatcherProvider
 * @see net.sfelabs.knox.core.testing.context.AndroidContextProviderRule
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PolicyListViewModelTest {

    // Use StandardTestDispatcher for controlled coroutine execution
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mock dependencies - these replace real Knox SDK calls
    private lateinit var mockPolicyRegistry: PolicyRegistry
    private lateinit var mockGroupingStrategy: PolicyGroupingStrategy
    private lateinit var mockLicenseInitializer: KnoxLicenseInitializer

    // Controllable flow for license status
    private lateinit var licenseStatusFlow: MutableStateFlow<LicenseStartupResult>

    // System under test
    private lateinit var viewModel: PolicyListViewModel

    /**
     * Set up test environment before each test.
     *
     * Key setup steps:
     * 1. Replace Dispatchers.Main with test dispatcher
     * 2. Create mock dependencies
     * 3. Configure default mock behaviors
     */
    @Before
    fun setup() {
        // CRITICAL: Replace Main dispatcher before creating ViewModel
        // ViewModels use viewModelScope which defaults to Dispatchers.Main
        Dispatchers.setMain(testDispatcher)

        // Create mocks for all dependencies
        mockPolicyRegistry = mockk(relaxed = true)
        mockGroupingStrategy = mockk(relaxed = true)
        mockLicenseInitializer = mockk(relaxed = true)

        // Create controllable license status flow
        licenseStatusFlow = MutableStateFlow<LicenseStartupResult>(
            LicenseStartupResult.NotChecked
        )
        every { mockLicenseInitializer.licenseStatus } returns licenseStatusFlow

        // Default: return empty groups
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns emptyList()
    }

    /**
     * Clean up after each test.
     *
     * CRITICAL: Always reset Main dispatcher to avoid affecting other tests.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state shows loading`() = testScope.runTest {
        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: Initial state should be loading
        // Note: We check BEFORE advanceUntilIdle to see the loading state
        assertTrue("Should be loading initially", viewModel.uiState.value.isLoading)
        assertEquals(emptyList<ResolvedPolicyGroup>(), viewModel.uiState.value.resolvedGroups)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial license state is false when not checked`() = testScope.runTest {
        // Given: License is not checked
        licenseStatusFlow.value = LicenseStartupResult.NotChecked

        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: License should not be activated
        assertFalse(viewModel.isLicenseActivated.value)
    }

    // ==================== Policy Loading Tests ====================

    @Test
    fun `loadPolicies updates state with resolved groups`() = testScope.runTest {
        // Given: Mock returns policy groups
        val testGroups = createTestPolicyGroups()
        coEvery { mockGroupingStrategy.resolveAllGroups(mockPolicyRegistry) } returns testGroups

        // When: ViewModel is created (which calls loadPolicies in init)
        viewModel = createViewModel()
        advanceUntilIdle()  // Wait for coroutines to complete

        // Then: State should have the groups and not be loading
        with(viewModel.uiState.value) {
            assertFalse("Should not be loading after load completes", isLoading)
            assertEquals(testGroups, resolvedGroups)
            assertNull("Should have no error on success", error)
        }
    }

    @Test
    fun `loadPolicies handles exceptions gracefully`() = testScope.runTest {
        // Given: Mock throws an exception
        val errorMessage = "Network error"
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } throws RuntimeException(errorMessage)

        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: State should show error
        with(viewModel.uiState.value) {
            assertFalse("Should not be loading after error", isLoading)
            assertEquals(errorMessage, error)
            assertTrue("Groups should be empty on error", resolvedGroups.isEmpty())
        }
    }

    @Test
    fun `loadPolicies can be called multiple times`() = testScope.runTest {
        // Given: First call returns groups, second call returns different groups
        val firstGroups = createTestPolicyGroups()
        val secondGroups = listOf(
            createTestGroup("updated", "Updated Group", 1)
        )

        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returnsMany listOf(
            firstGroups,
            secondGroups
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        // Verify first load
        assertEquals(firstGroups, viewModel.uiState.value.resolvedGroups)

        // When: Reload policies
        viewModel.loadPolicies()
        advanceUntilIdle()

        // Then: State should have new groups
        assertEquals(secondGroups, viewModel.uiState.value.resolvedGroups)
    }

    // ==================== Search/Filter Tests ====================

    @Test
    fun `onSearchQueryChanged updates search query in state`() = testScope.runTest {
        // Given: ViewModel with loaded policies
        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Search query is changed
        viewModel.onSearchQueryChanged("test query")

        // Then: State should reflect the new query
        assertEquals("test query", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `getFilteredGroups returns all groups when query is empty`() = testScope.runTest {
        // Given: ViewModel with loaded policies
        val testGroups = createTestPolicyGroups()
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns testGroups

        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Query is empty
        viewModel.onSearchQueryChanged("")

        // Then: All groups should be returned
        assertEquals(testGroups, viewModel.getFilteredGroups())
    }

    @Test
    fun `getFilteredGroups filters by policy title`() = testScope.runTest {
        // Given: Groups with policies that have specific titles
        val matchingPolicy = createMockPolicy("wifi_policy", "WiFi Settings", "Configure WiFi")
        val nonMatchingPolicy = createMockPolicy("bluetooth_policy", "Bluetooth", "Configure Bluetooth")

        val groups = listOf(
            ResolvedPolicyGroup(
                group = PolicyGroup(id = "connectivity", displayName = "Connectivity"),
                policies = listOf(matchingPolicy, nonMatchingPolicy)
            )
        )
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns groups

        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Search for "wifi"
        viewModel.onSearchQueryChanged("wifi")

        // Then: Only WiFi policy should be in results
        val filtered = viewModel.getFilteredGroups()
        assertEquals(1, filtered.size)
        assertEquals(1, filtered[0].policies.size)
        assertEquals("wifi_policy", filtered[0].policies[0].policyName)
    }

    @Test
    fun `getFilteredGroups filters by policy description`() = testScope.runTest {
        // Given: Policy with searchable description
        val policy = createMockPolicy("test_policy", "Generic Title", "Enable secure connections")

        val groups = listOf(
            ResolvedPolicyGroup(
                group = PolicyGroup(id = "security", displayName = "Security"),
                policies = listOf(policy)
            )
        )
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns groups

        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Search for term in description
        viewModel.onSearchQueryChanged("secure")

        // Then: Policy should be found
        val filtered = viewModel.getFilteredGroups()
        assertEquals(1, filtered.size)
        assertEquals(1, filtered[0].policies.size)
    }

    @Test
    fun `getFilteredGroups is case insensitive`() = testScope.runTest {
        // Given: Policy with mixed case title
        val policy = createMockPolicy("wifi", "WiFi Settings", "Description")

        val groups = listOf(
            ResolvedPolicyGroup(
                group = PolicyGroup(id = "conn", displayName = "Connectivity"),
                policies = listOf(policy)
            )
        )
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns groups

        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Search with different case
        viewModel.onSearchQueryChanged("WIFI")

        // Then: Should still find the policy
        val filtered = viewModel.getFilteredGroups()
        assertEquals(1, filtered[0].policies.size)
    }

    @Test
    fun `getFilteredGroups removes empty groups`() = testScope.runTest {
        // Given: Two groups where one will have no matching policies
        val matchingPolicy = createMockPolicy("wifi", "WiFi", "Wireless")
        val nonMatchingPolicy = createMockPolicy("camera", "Camera", "Photos")

        val groups = listOf(
            ResolvedPolicyGroup(
                group = PolicyGroup(id = "conn", displayName = "Connectivity"),
                policies = listOf(matchingPolicy)
            ),
            ResolvedPolicyGroup(
                group = PolicyGroup(id = "media", displayName = "Media"),
                policies = listOf(nonMatchingPolicy)
            )
        )
        coEvery { mockGroupingStrategy.resolveAllGroups(any()) } returns groups

        viewModel = createViewModel()
        advanceUntilIdle()

        // When: Search for "wifi"
        viewModel.onSearchQueryChanged("wifi")

        // Then: Only connectivity group should remain
        val filtered = viewModel.getFilteredGroups()
        assertEquals(1, filtered.size)
        assertEquals("conn", filtered[0].group.id)
    }

    // ==================== License State Tests ====================
    //
    // NOTE: The isLicenseActivated StateFlow uses SharingStarted.WhileSubscribed(5000),
    // which means it only starts collecting from the upstream flow when there's an
    // active subscriber. In tests, we need to either:
    // 1. Use backgroundScope.launch to collect from the flow
    // 2. Use first() to trigger collection and get the value
    //
    // These tests demonstrate both approaches.

    @Test
    fun `isLicenseActivated is true when AlreadyActivated`() = testScope.runTest {
        // Given: License is already activated
        licenseStatusFlow.value = LicenseStartupResult.AlreadyActivated

        // When: ViewModel is created
        viewModel = createViewModel()

        // Start collecting to activate the WhileSubscribed flow
        backgroundScope.launch { viewModel.isLicenseActivated.collect {} }
        advanceUntilIdle()

        // Then: License should be activated
        assertTrue(viewModel.isLicenseActivated.value)
    }

    @Test
    fun `isLicenseActivated is true when ActivatedNow`() = testScope.runTest {
        // Given: License was just activated
        licenseStatusFlow.value = LicenseStartupResult.ActivatedNow

        // When: ViewModel is created
        viewModel = createViewModel()

        // Start collecting to activate the WhileSubscribed flow
        backgroundScope.launch { viewModel.isLicenseActivated.collect {} }
        advanceUntilIdle()

        // Then: License should be activated
        assertTrue(viewModel.isLicenseActivated.value)
    }

    @Test
    fun `isLicenseActivated is false for non-activated states`() = testScope.runTest {
        // Given: Various non-activated states
        val nonActivatedStates = listOf(
            LicenseStartupResult.NotChecked,
            LicenseStartupResult.ActivationFailed("Test failure"),
            LicenseStartupResult.InitializationError("Init error")
        )

        for (state in nonActivatedStates) {
            licenseStatusFlow.value = state

            viewModel = createViewModel()

            // Start collecting to activate the WhileSubscribed flow
            backgroundScope.launch { viewModel.isLicenseActivated.collect {} }
            advanceUntilIdle()

            assertFalse(
                "License should not be activated for state: $state",
                viewModel.isLicenseActivated.value
            )
        }
    }

    @Test
    fun `isLicenseActivated updates when license status changes`() = testScope.runTest {
        // Given: Start with not checked
        licenseStatusFlow.value = LicenseStartupResult.NotChecked

        viewModel = createViewModel()

        // Start collecting to activate the WhileSubscribed flow
        backgroundScope.launch { viewModel.isLicenseActivated.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.isLicenseActivated.value)

        // When: License becomes activated
        licenseStatusFlow.value = LicenseStartupResult.AlreadyActivated
        advanceUntilIdle()

        // Then: isLicenseActivated should update
        assertTrue(viewModel.isLicenseActivated.value)
    }

    // ==================== Helper Methods ====================

    /**
     * Factory method to create ViewModel with mocked dependencies.
     *
     * This pattern keeps tests clean and allows customization per test.
     */
    private fun createViewModel(): PolicyListViewModel {
        return PolicyListViewModel(
            policyRegistry = mockPolicyRegistry,
            groupingStrategy = mockGroupingStrategy,
            licenseInitializer = mockLicenseInitializer
        )
    }

    /**
     * Creates test policy groups for testing.
     */
    private fun createTestPolicyGroups(): List<ResolvedPolicyGroup> {
        return listOf(
            createTestGroup("security", "Security", 3),
            createTestGroup("connectivity", "Connectivity", 2)
        )
    }

    /**
     * Creates a single test group with mock policies.
     */
    private fun createTestGroup(id: String, name: String, policyCount: Int): ResolvedPolicyGroup {
        val policies = (1..policyCount).map { i ->
            createMockPolicy("${id}_policy_$i", "$name Policy $i", "Description for policy $i")
        }
        return ResolvedPolicyGroup(
            group = PolicyGroup(id = id, displayName = name),
            policies = policies
        )
    }

    /**
     * Creates a mock PolicyComponent for testing.
     *
     * Note: PolicyComponent is an interface from knox-core, so we use MockK
     * to create a fake implementation that returns the values we specify.
     */
    private fun createMockPolicy(
        name: String,
        title: String,
        description: String
    ): PolicyComponent<out PolicyState> {
        return mockk<PolicyComponent<PolicyState>> {
            every { policyName } returns name
            every { this@mockk.title } returns title
            every { this@mockk.description } returns description
            every { category } returns PolicyCategory.Toggle
            every { capabilities } returns emptySet()
        }
    }
}
