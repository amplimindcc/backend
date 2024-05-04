package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.exceptions.ResourceNotFoundException
import de.amplimind.codingchallenge.model.InviteTokenExpiration
import de.amplimind.codingchallenge.repository.InviteTokenExpirationRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.springframework.test.context.ActiveProfiles
import java.util.Date

/**
 * Test class for [InviteTokenExpirationService].
 */
@ActiveProfiles("test")
class InviteTokenExpirationServiceTest {
    @MockK
    private lateinit var inviteTokenExpirationRepository: InviteTokenExpirationRepository

    @InjectMockKs
    private lateinit var inviteTokenExpirationService: InviteTokenExpirationService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    /**
     * Test that an exception is thrown when the expiration date for a user cannot be found.
     */
    @Test
    fun test_fetch_expiration_date_fail() {
        every { inviteTokenExpirationRepository.findByEmail(any()) } returns null
        assertThrows<ResourceNotFoundException> { this.inviteTokenExpirationService.fetchExpirationDateForUser("unknown@web.de") }
    }

    /**
     * Test that the expiration date for a user is fetched successfully, if there exists one
     */
    @Test
    fun test_fetch_expiration_date_success() {
        val email = "someuser@web.de"

        every { inviteTokenExpirationRepository.findByEmail(any()) } returns
            InviteTokenExpiration(
                email = email,
                expirationInMillis = Date.from(Date().toInstant().plusSeconds(60 * 60 * 24 * 7)).time,
            )
        assertDoesNotThrow { this.inviteTokenExpirationService.fetchExpirationDateForUser(email) }
    }

    /**
     * Test that the expiration token is correctly updated in the persistence
     */
    @Test
    fun test_update_expiration_token_success() {
        val email = "someuser@web.de"

        every { inviteTokenExpirationRepository.findByEmail(any()) } returns null

        val tokenSlot = slot<InviteTokenExpiration>()
        every { inviteTokenExpirationRepository.save(capture(tokenSlot)) } returns any()

        this.inviteTokenExpirationService.updateExpirationToken(email, Date.from(Date().toInstant().plusSeconds(60 * 60 * 24 * 7)).time)

        assert(tokenSlot.captured.email == email)
        verify { inviteTokenExpirationRepository.save(any()) }
    }
}
