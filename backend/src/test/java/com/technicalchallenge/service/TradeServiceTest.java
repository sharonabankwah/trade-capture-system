package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Cashflow;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.LegType;
import com.technicalchallenge.model.Schedule;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CashflowRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.ScheduleRepository;
import com.technicalchallenge.repository.TradeLegRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.TradeTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private TradeTypeService tradeTypeService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;

    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);


        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);


        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
        // Set version to 1 
        trade.setVersion(1);

    }



    @Test
    void testCreateTrade_Success() {
        // Given
        tradeDTO.setBookName("Book-1");
        tradeDTO.setCounterpartyName("Counterparty-1");
        tradeDTO.setTradeStatus("NEW");

        // Mocked dependencies
        Book mockBook = new Book();
        Counterparty mockCounterparty = new Counterparty();
        TradeStatus mockStatus = new TradeStatus();
        TradeLeg mockLeg = new TradeLeg();

        // Set IDs for mocks
        mockBook.setId(1L);
        mockCounterparty.setId(1L);
        mockStatus.setTradeStatus("NEW");
        mockLeg.setLegId(1L);

        // Mocked repository methods
        when(bookRepository.findByBookName("Book-1")).thenReturn(Optional.of(mockBook));
        when(counterpartyRepository.findByName("Counterparty-1")).thenReturn(Optional.of(mockCounterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(mockStatus));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(mockLeg);
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // Fixed the expected message
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {
        // Given
        // Added mock TradeLeg
        TradeLeg mockLeg = new TradeLeg();
        // Set legId
        mockLeg.setLegId(1L);

        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        // Mocked repository to return mockLeg 
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(mockLeg);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

      // This test has a deliberate bug for candidates to find and fix
      @Test
      void testCashflowGeneration_MonthlySchedule() {
  
          // Given

          // Mocked dependencies

          Book mockBook = new Book();
          Counterparty mockCounterparty = new Counterparty();
          TradeStatus mockStatus = new TradeStatus();
          Cashflow mockCashflow = new Cashflow();

          mockBook.setBookName("Book-1");
          mockCounterparty.setName("Counterparty-1");
          mockStatus.setTradeStatus("NEW");

          // TradeDTO setup 

          tradeDTO.setBookName("Book-1");
          tradeDTO.setCounterpartyName("Counterparty-1");
          tradeDTO.setTradeStatus("NEW");
          tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 15));
          tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 15));

          // TradeLegDTO setup

          TradeLegDTO leg1 = new TradeLegDTO();
          leg1.setNotional(BigDecimal.valueOf(1000000));
          leg1.setRate(0.05);

          TradeLegDTO leg2 = new TradeLegDTO();
          leg2.setNotional(BigDecimal.valueOf(1000000));
          leg2.setRate(0.05);

          Schedule schedule = new Schedule();
          schedule.setSchedule("1M");
          schedule.setId(123L);

          // Added legs to tradeDTO

          tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

          TradeLeg savedLeg1 = new TradeLeg();
          savedLeg1.setLegId(1L);
          savedLeg1.setNotional(BigDecimal.valueOf(1000000));
          savedLeg1.setRate(0.05);
          savedLeg1.setCalculationPeriodSchedule(schedule);

          TradeLeg savedLeg2 = new TradeLeg();
          savedLeg2.setLegId(1L);
          savedLeg2.setNotional(BigDecimal.valueOf(1000000));
          savedLeg2.setRate(0.05);
          savedLeg2.setCalculationPeriodSchedule(schedule);

          Trade tradeEntity = new Trade();
          tradeEntity.setTradeId(10001L);
          tradeEntity.setActive(true);
          tradeEntity.setTradeLegs(Arrays.asList(savedLeg1, savedLeg2));

          // Mocked repositories

          when(bookRepository.findByBookName("Book-1")).thenReturn(Optional.of(mockBook));
          when(counterpartyRepository.findByName("Counterparty-1")).thenReturn(Optional.of(mockCounterparty));
          when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(mockStatus));
          when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(savedLeg1);
          when(tradeRepository.save(any(Trade.class))).thenReturn(tradeEntity);
          when(cashflowRepository.save(any(Cashflow.class))).thenReturn(mockCashflow);
  
          // When
          Trade result = tradeService.createTrade(tradeDTO);
  
          // Then
          assertNotNull(result);
          assertEquals(2, result.getTradeLegs().size());
          verify(cashflowRepository, atLeastOnce()).save(any(Cashflow.class)); 
      }
  }