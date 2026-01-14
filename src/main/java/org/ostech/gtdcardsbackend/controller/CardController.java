package org.ostech.gtdcardsbackend.controller;

import jakarta.validation.Valid;
import org.ostech.gtdcardsbackend.dto.*;
import org.ostech.gtdcardsbackend.service.CardService;
import org.ostech.gtdcardsbackend.util.APIConstants;
import org.ostech.gtdcardsbackend.util.MessageConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(APIConstants.CARD_ENDPOINT)
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CardResponseDTO>> createCard(
        @Valid @RequestBody CardRequestDTO cardRequestDTO) {
        CardResponseDTO cardResponseDTO = cardService.createCard(cardRequestDTO);
        ApiResponseDTO<CardResponseDTO> response = ApiResponseDTO.success(
            cardResponseDTO,
            MessageConstants.CARD_CREATED_SUCCESS
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<CardResponseDTO>>> getAllCards(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size, //TODO: Move to configuration/ DB
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponseDTO<CardResponseDTO> pageResponse = cardService.getAllCards(pageable);
        ApiResponseDTO<PageResponseDTO<CardResponseDTO>> response = ApiResponseDTO.success(
            pageResponse,
            "Cards retrieved successfully"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(APIConstants.CARD_GET_BY_ID_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<CardResponseDTO>> getCardById(
        @PathVariable Long id) {
        CardResponseDTO cardResponseDTO = cardService.getCardById(id);
        ApiResponseDTO<CardResponseDTO> response = ApiResponseDTO.success(
            cardResponseDTO,
            "Card retrieved successfully"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(APIConstants.CARD_UPDATE_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<CardResponseDTO>> updateCard(
        @PathVariable Long id,
        @Valid @RequestBody CardUpdateDTO cardUpdateDTO) {
        CardResponseDTO cardResponseDTO = cardService.updateCard(id, cardUpdateDTO);
        ApiResponseDTO<CardResponseDTO> response = ApiResponseDTO.success(
            cardResponseDTO,
            MessageConstants.CARD_UPDATED_SUCCESS
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
