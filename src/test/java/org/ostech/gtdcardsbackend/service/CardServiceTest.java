package org.ostech.gtdcardsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ostech.gtdcardsbackend.dto.CardRequestDTO;
import org.ostech.gtdcardsbackend.dto.CardResponseDTO;
import org.ostech.gtdcardsbackend.dto.CardUpdateDTO;
import org.ostech.gtdcardsbackend.enums.CardStatus;
import org.ostech.gtdcardsbackend.enums.CardType;
import org.ostech.gtdcardsbackend.exception.CardAlreadyExistsException;
import org.ostech.gtdcardsbackend.exception.CardNotFoundException;
import org.ostech.gtdcardsbackend.exception.CardServiceException;
import org.ostech.gtdcardsbackend.model.Card;
import org.ostech.gtdcardsbackend.repository.CardRepository;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private CardRequestDTO cardRequestDTO;
    private CardUpdateDTO cardUpdateDTO;
    private Card mockCard;

    @BeforeEach
    void setUp() {
        cardRequestDTO = CardRequestDTO.builder()
            .cardNumber("1234567890123456")
            .holderName("John Doe")
            .cardType("CREDIT")
            .cvv("123")
            .expiryDate("12/25")
            .build();

        cardUpdateDTO = CardUpdateDTO.builder()
            .holderName("Jane Doe")
            .status("BLOCKED")
            .build();

        mockCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("John Doe")
            .cardType(CardType.CREDIT)
            .status(CardStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("1. Should create card successfully with valid data")
    void testCreateCard_WithValidData_ReturnsCardResponseDTO() {
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);

        CardResponseDTO response = cardService.createCard(cardRequestDTO);

        assertThat(response)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("holderName", "John Doe")
            .hasFieldOrPropertyWithValue("cardType", "CREDIT")
            .hasFieldOrPropertyWithValue("status", "ACTIVE");

        assertThat(response.getMaskedCardNumber())
            .startsWith("*")
            .endsWith("3456")
            .doesNotContain("1234567890");

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("2. Should throw CardAlreadyExistsException when card number already exists")
    void testCreateCard_WithDuplicateCardNumber_ThrowsCardAlreadyExistsException() {
        when(cardRepository.save(any(Card.class)))
            .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThatThrownBy(() -> cardService.createCard(cardRequestDTO))
            .isInstanceOf(CardAlreadyExistsException.class)
            .hasMessageContaining("already exists")
            .hasMessageContaining("****3456");

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("3. Should set default status to ACTIVE when creating card")
    void testCreateCard_SetsDefaultStatusToActive() {
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);

        CardResponseDTO response = cardService.createCard(cardRequestDTO);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");

        verify(cardRepository).save(argThat(card ->
            card.getStatus() == CardStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("4. Should throw CardServiceException on unexpected database error")
    void testCreateCard_OnDatabaseError_ThrowsCardServiceException() {
        when(cardRepository.save(any(Card.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> cardService.createCard(cardRequestDTO))
            .isInstanceOf(CardServiceException.class)
            .hasMessageContaining("Error creating card");

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("5. Should mask card number in response (PCI Compliance)")
    void testCreateCard_MasksCardNumberInResponse() {
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);

        CardResponseDTO response = cardService.createCard(cardRequestDTO);

        String maskedCardNumber = response.getMaskedCardNumber();

        assertThat(maskedCardNumber)
            .hasSize(16)  // Same length as original
            .startsWith("*")
            .contains("3456")  // Last 4 digits visible
            .doesNotContain("1234")
            .doesNotContain("5678")
            .doesNotContain("9012");
    }

    @Test
    @DisplayName("1. Should update card successfully with valid data")
    void testUpdateCard_WithValidData_ReturnsUpdatedCardResponseDTO() {
        Card updatedCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("Jane Doe")
            .cardType(CardType.CREDIT)
            .status(CardStatus.BLOCKED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        CardResponseDTO response = cardService.updateCard(1L, cardUpdateDTO);

        assertThat(response)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("holderName", "Jane Doe")
            .hasFieldOrPropertyWithValue("status", "BLOCKED");

        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("2. Should throw CardNotFoundException when card does not exist")
    void testUpdateCard_WithNonExistentId_ThrowsCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(99L, cardUpdateDTO))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Card not found");

        verify(cardRepository, times(1)).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("3. Should update only holderName when status is null")
    void testUpdateCard_UpdateOnlyHolderName_LeavesStatusUnchanged() {
        CardUpdateDTO partialUpdate = CardUpdateDTO.builder()
            .holderName("New Name")
            .status(null)
            .build();

        Card expectedCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("New Name")
            .cardType(CardType.CREDIT)
            .status(CardStatus.ACTIVE)  // Unchanged
            .createdAt(mockCard.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        CardResponseDTO response = cardService.updateCard(1L, partialUpdate);

        assertThat(response)
            .hasFieldOrPropertyWithValue("holderName", "New Name")
            .hasFieldOrPropertyWithValue("status", "ACTIVE");

        verify(cardRepository).save(argThat(card ->
            card.getHolderName().equals("New Name") &&
                card.getStatus() == CardStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("4. Should update only status when holderName is null")
    void testUpdateCard_UpdateOnlyStatus_LeavesHolderNameUnchanged() {
        CardUpdateDTO partialUpdate = CardUpdateDTO.builder()
            .holderName(null)
            .status("BLOCKED")
            .build();

        Card expectedCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("John Doe")  // Unchanged
            .cardType(CardType.CREDIT)
            .status(CardStatus.BLOCKED)
            .createdAt(mockCard.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        CardResponseDTO response = cardService.updateCard(1L, partialUpdate);

        assertThat(response)
            .hasFieldOrPropertyWithValue("holderName", "John Doe")
            .hasFieldOrPropertyWithValue("status", "BLOCKED");

        verify(cardRepository).save(argThat(card ->
            card.getHolderName().equals("John Doe") &&
                card.getStatus() == CardStatus.BLOCKED
        ));
    }

    @Test
    @DisplayName("5. Should throw CardServiceException on database error during update")
    void testUpdateCard_OnDatabaseError_ThrowsCardServiceException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class)))
            .thenThrow(new RuntimeException("Database connection lost"));

        assertThatThrownBy(() -> cardService.updateCard(1L, cardUpdateDTO))
            .isInstanceOf(CardServiceException.class)
            .hasMessageContaining("Error updating card");

        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should not update card with blank holderName")
    void testUpdateCard_WithBlankHolderName_SkipsUpdate() {
        CardUpdateDTO blankNameUpdate = CardUpdateDTO.builder()
            .holderName("")  // Blank
            .status("BLOCKED")
            .build();

        Card expectedCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("John Doe")  // Unchanged
            .cardType(CardType.CREDIT)
            .status(CardStatus.BLOCKED)
            .createdAt(mockCard.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        CardResponseDTO response = cardService.updateCard(1L, blankNameUpdate);

        assertThat(response.getHolderName()).isEqualTo("John Doe");
        assertThat(response.getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Should not update card with blank status")
    void testUpdateCard_WithBlankStatus_SkipsUpdate() {
        CardUpdateDTO blankStatusUpdate = CardUpdateDTO.builder()
            .holderName("Jane Doe")
            .status("")  // Blank
            .build();

        Card expectedCard = Card.builder()
            .id(1L)
            .cardNumber("1234567890123456")
            .holderName("Jane Doe")
            .cardType(CardType.CREDIT)
            .status(CardStatus.ACTIVE)  // Unchanged
            .createdAt(mockCard.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        CardResponseDTO response = cardService.updateCard(1L, blankStatusUpdate);

        assertThat(response.getHolderName()).isEqualTo("Jane Doe");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }
}
