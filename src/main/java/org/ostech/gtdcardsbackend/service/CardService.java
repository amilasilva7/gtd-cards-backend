package org.ostech.gtdcardsbackend.service;

import org.ostech.gtdcardsbackend.dto.CardRequestDTO;
import org.ostech.gtdcardsbackend.dto.CardResponseDTO;
import org.ostech.gtdcardsbackend.dto.CardUpdateDTO;
import org.ostech.gtdcardsbackend.dto.PageResponseDTO;
import org.ostech.gtdcardsbackend.enums.CardStatus;
import org.ostech.gtdcardsbackend.enums.CardType;
import org.ostech.gtdcardsbackend.exception.CardAlreadyExistsException;
import org.ostech.gtdcardsbackend.exception.CardNotFoundException;
import org.ostech.gtdcardsbackend.exception.CardServiceException;
import org.ostech.gtdcardsbackend.model.Card;
import org.ostech.gtdcardsbackend.repository.CardRepository;
import org.ostech.gtdcardsbackend.util.CardMaskingUtil;
import org.ostech.gtdcardsbackend.util.MessageConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public CardResponseDTO createCard(CardRequestDTO cardRequestDTO) {
        try {
            Card card = Card.builder()
                .cardNumber(cardRequestDTO.getCardNumber())
                .holderName(cardRequestDTO.getHolderName())
                .cardType(CardType.valueOf(cardRequestDTO.getCardType()))
                .status(CardStatus.ACTIVE)
                .build();

            Card savedCard = cardRepository.save(card);
            return mapCardToResponseDTO(savedCard);

        } catch (DataIntegrityViolationException e) {
            String maskedCardNumber = CardMaskingUtil.maskCardNumber(cardRequestDTO.getCardNumber());
            throw new CardAlreadyExistsException("Card with number " + maskedCardNumber + " already exists");
        } catch (Exception e) {
            throw new CardServiceException("Error creating card: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public CardResponseDTO getCardById(Long cardId) {
        try {
            Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(MessageConstants.CARD_NOT_FOUND));
            return mapCardToResponseDTO(card);
        } catch (CardNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CardServiceException("Error retrieving card: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CardResponseDTO> getAllCards(Pageable pageable) {
        try {
            Page<Card> page = cardRepository.findAll(pageable);
            return PageResponseDTO.<CardResponseDTO>builder()
                .content(page.getContent().stream()
                    .map(this::mapCardToResponseDTO)
                    .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
        } catch (Exception e) {
            throw new CardServiceException("Error retrieving cards: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CardResponseDTO updateCard(Long cardId, CardUpdateDTO cardUpdateDTO) {
        try {
            Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(MessageConstants.CARD_NOT_FOUND));

            if (cardUpdateDTO.getHolderName() != null && !cardUpdateDTO.getHolderName().isBlank()) {
                card.setHolderName(cardUpdateDTO.getHolderName());
            }

            if (cardUpdateDTO.getStatus() != null && !cardUpdateDTO.getStatus().isBlank()) {
                card.setStatus(CardStatus.valueOf(cardUpdateDTO.getStatus()));
            }

            Card updatedCard = cardRepository.save(card);
            return mapCardToResponseDTO(updatedCard);

        } catch (CardNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CardServiceException("Error updating card: " + e.getMessage(), e);
        }
    }

    private CardResponseDTO mapCardToResponseDTO(Card card) {
        return CardResponseDTO.builder()
            .id(card.getId())
            .maskedCardNumber(CardMaskingUtil.maskCardNumber(card.getCardNumber()))
            .holderName(card.getHolderName())
            .cardType(card.getCardType().toString())
            .status(card.getStatus().toString())
            .createdAt(card.getCreatedAt())
            .updatedAt(card.getUpdatedAt())
            .build();
    }
}
