package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.mapper.PlantReminderMapper;
import dev.solace.twiggle.model.PlantReminder;
import dev.solace.twiggle.repository.PlantReminderRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PlantReminderServiceTest {

    @Mock
    private PlantReminderRepository plantReminderRepository;

    @Mock
    private PlantReminderMapper plantReminderMapper;

    @InjectMocks
    private PlantReminderService plantReminderService;

    private UUID reminderId;
    private PlantReminder reminder;
    private PlantReminderDTO reminderDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reminderId = UUID.randomUUID();
        reminder = new PlantReminder();
        reminder.setId(reminderId);
        reminderDTO = PlantReminderDTO.builder()
                .plantId(UUID.randomUUID())
                .gardenPlanId(UUID.randomUUID())
                .reminderType("Water")
                .reminderDate(LocalDate.now())
                .isCompleted(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testFindById() {
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Optional<PlantReminderDTO> result = plantReminderService.findById(reminderId);
        assertTrue(result.isPresent());
        assertEquals(reminderDTO, result.get());
    }

    @Test
    void testCreate() {
        when(plantReminderMapper.toEntity(any())).thenReturn(reminder);
        when(plantReminderRepository.save(any())).thenReturn(reminder);
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        PlantReminderDTO result = plantReminderService.create(reminderDTO);
        assertNotNull(result);
        assertEquals("Water", result.getReminderType());
    }

    @Test
    void testUpdate() {
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));
        when(plantReminderRepository.save(any())).thenReturn(reminder);
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Optional<PlantReminderDTO> result = plantReminderService.update(reminderId, reminderDTO);
        assertTrue(result.isPresent());
        assertEquals(reminderDTO, result.get());
    }

    @Test
    void testMarkAsCompleted() {
        reminder.setIsCompleted(false);
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));
        when(plantReminderRepository.save(any())).thenReturn(reminder);
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Optional<PlantReminderDTO> result = plantReminderService.markAsCompleted(reminderId);
        assertTrue(result.isPresent());
        assertEquals(reminderDTO, result.get());
    }

    @Test
    void testFindAllPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plantReminderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(reminder)));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Page<PlantReminderDTO> result = plantReminderService.findAll(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testDelete() {
        doNothing().when(plantReminderRepository).deleteById(reminderId);
        assertDoesNotThrow(() -> plantReminderService.delete(reminderId));
    }
}
