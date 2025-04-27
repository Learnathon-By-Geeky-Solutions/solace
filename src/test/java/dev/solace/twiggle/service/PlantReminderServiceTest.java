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
    private UUID plantId;
    private UUID gardenPlanId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reminderId = UUID.randomUUID();
        plantId = UUID.randomUUID();
        gardenPlanId = UUID.randomUUID();

        reminder = new PlantReminder();
        reminder.setId(reminderId);
        reminder.setPlantId(plantId);
        reminder.setGardenPlanId(gardenPlanId);
        reminder.setReminderType("Water");
        reminder.setReminderDate(LocalDate.now());
        reminder.setIsCompleted(false);

        reminderDTO = PlantReminderDTO.builder()
                .plantId(plantId)
                .gardenPlanId(gardenPlanId)
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
    void testFindByIdNotFound() {
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.empty());

        Optional<PlantReminderDTO> result = plantReminderService.findById(reminderId);
        assertFalse(result.isPresent());
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
    void testCreateWithNullIsCompleted() {
        PlantReminderDTO inputDTO = PlantReminderDTO.builder()
                .plantId(plantId)
                .gardenPlanId(gardenPlanId)
                .reminderType("Water")
                .reminderDate(LocalDate.now())
                .isCompleted(null)
                .build();

        PlantReminder entity = new PlantReminder();
        entity.setId(reminderId);
        entity.setPlantId(plantId);
        entity.setGardenPlanId(gardenPlanId);
        entity.setReminderType("Water");
        entity.setReminderDate(LocalDate.now());
        entity.setIsCompleted(false);

        PlantReminderDTO expectedDTO = PlantReminderDTO.builder()
                .plantId(plantId)
                .gardenPlanId(gardenPlanId)
                .reminderType("Water")
                .reminderDate(LocalDate.now())
                .isCompleted(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(plantReminderMapper.toEntity(any())).thenReturn(entity);
        when(plantReminderRepository.save(any())).thenReturn(entity);
        when(plantReminderMapper.toDto(entity)).thenReturn(expectedDTO);

        PlantReminderDTO result = plantReminderService.create(inputDTO);
        assertNotNull(result);
        assertFalse(result.getIsCompleted());
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
    void testUpdateNotFound() {
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.empty());

        Optional<PlantReminderDTO> result = plantReminderService.update(reminderId, reminderDTO);
        assertFalse(result.isPresent());
    }

    @Test
    void testMarkAsCompleted() {
        reminder.setIsCompleted(false);
        PlantReminder updatedReminder = new PlantReminder();
        updatedReminder.setId(reminderId);
        updatedReminder.setPlantId(plantId);
        updatedReminder.setGardenPlanId(gardenPlanId);
        updatedReminder.setReminderType("Water");
        updatedReminder.setReminderDate(LocalDate.now());
        updatedReminder.setIsCompleted(true);

        PlantReminderDTO updatedDTO = PlantReminderDTO.builder()
                .plantId(plantId)
                .gardenPlanId(gardenPlanId)
                .reminderType("Water")
                .reminderDate(LocalDate.now())
                .isCompleted(true)
                .createdAt(OffsetDateTime.now())
                .build();

        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));
        when(plantReminderRepository.save(any())).thenReturn(updatedReminder);
        when(plantReminderMapper.toDto(updatedReminder)).thenReturn(updatedDTO);

        Optional<PlantReminderDTO> result = plantReminderService.markAsCompleted(reminderId);
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsCompleted());
    }

    @Test
    void testMarkAsCompletedNotFound() {
        when(plantReminderRepository.findById(reminderId)).thenReturn(Optional.empty());

        Optional<PlantReminderDTO> result = plantReminderService.markAsCompleted(reminderId);
        assertFalse(result.isPresent());
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
    void testFindAll() {
        when(plantReminderRepository.findAll()).thenReturn(List.of(reminder));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        List<PlantReminderDTO> result = plantReminderService.findAll();
        assertEquals(1, result.size());
        assertEquals(reminderDTO, result.getFirst());
    }

    @Test
    void testFindByPlantIdWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plantReminderRepository.findByPlantId(plantId, pageable)).thenReturn(new PageImpl<>(List.of(reminder)));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Page<PlantReminderDTO> result = plantReminderService.findByPlantId(plantId, pageable);
        assertEquals(1, result.getContent().size());
        assertEquals(reminderDTO, result.getContent().getFirst());
    }

    @Test
    void testFindByPlantId() {
        when(plantReminderRepository.findByPlantId(plantId)).thenReturn(List.of(reminder));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        List<PlantReminderDTO> result = plantReminderService.findByPlantId(plantId);
        assertEquals(1, result.size());
        assertEquals(reminderDTO, result.getFirst());
    }

    @Test
    void testFindByGardenPlanIdWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plantReminderRepository.findByGardenPlanId(gardenPlanId, pageable))
                .thenReturn(new PageImpl<>(List.of(reminder)));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Page<PlantReminderDTO> result = plantReminderService.findByGardenPlanId(gardenPlanId, pageable);
        assertEquals(1, result.getContent().size());
        assertEquals(reminderDTO, result.getContent().getFirst());
    }

    @Test
    void testFindByGardenPlanId() {
        when(plantReminderRepository.findByGardenPlanId(gardenPlanId)).thenReturn(List.of(reminder));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        List<PlantReminderDTO> result = plantReminderService.findByGardenPlanId(gardenPlanId);
        assertEquals(1, result.size());
        assertEquals(reminderDTO, result.getFirst());
    }

    @Test
    void testFindByPlantIdAndIsCompleted() {
        when(plantReminderRepository.findByPlantIdAndIsCompleted(plantId, true)).thenReturn(List.of(reminder));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        List<PlantReminderDTO> result = plantReminderService.findByPlantIdAndIsCompleted(plantId, true);
        assertEquals(1, result.size());
        assertEquals(reminderDTO, result.getFirst());
    }

    @Test
    void testFindByReminderDateLessThanEqual() {
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        when(plantReminderRepository.findByReminderDateLessThanEqual(date, pageable))
                .thenReturn(new PageImpl<>(List.of(reminder)));
        when(plantReminderMapper.toDto(reminder)).thenReturn(reminderDTO);

        Page<PlantReminderDTO> result = plantReminderService.findByReminderDateLessThanEqual(date, pageable);
        assertEquals(1, result.getContent().size());
        assertEquals(reminderDTO, result.getContent().getFirst());
    }

    @Test
    void testDelete() {
        doNothing().when(plantReminderRepository).deleteById(reminderId);
        assertDoesNotThrow(() -> plantReminderService.delete(reminderId));
    }
}
