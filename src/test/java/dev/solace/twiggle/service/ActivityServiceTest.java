package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.ActivityDTO;
import dev.solace.twiggle.mapper.ActivityMapper;
import dev.solace.twiggle.model.Activity;
import dev.solace.twiggle.repository.ActivityRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityService activityService;

    private Activity activity;
    private ActivityDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID userId = UUID.randomUUID();
        UUID gardenPlanId = UUID.randomUUID();
        activity = new Activity(
                UUID.randomUUID(), userId, gardenPlanId, "WATERING", "Watered the garden", OffsetDateTime.now());
        dto = new ActivityDTO(userId, gardenPlanId, "WATERING", "Watered the garden", OffsetDateTime.now());
    }

    @Test
    void findAll_shouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        when(activityRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(activity)));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Page<ActivityDTO> result = activityService.findAll(pageable);

        assertThat(result).hasSize(1);
        verify(activityRepository).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(activityRepository.findAll()).thenReturn(List.of(activity));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        List<ActivityDTO> result = activityService.findAll();

        assertThat(result).hasSize(1);
        verify(activityRepository).findAll();
    }

    @Test
    void findById_shouldReturnDTO() {
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Optional<ActivityDTO> result = activityService.findById(activity.getId());

        assertThat(result).isPresent();
        verify(activityRepository).findById(activity.getId());
    }

    @Test
    void findByUserId_shouldReturnList() {
        when(activityRepository.findByUserId(activity.getUserId())).thenReturn(List.of(activity));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        List<ActivityDTO> result = activityService.findByUserId(activity.getUserId());

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserId_withPageable_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(activityRepository.findByUserId(activity.getUserId(), pageable))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Page<ActivityDTO> result = activityService.findByUserId(activity.getUserId(), pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByGardenPlanId_shouldReturnList() {
        when(activityRepository.findByGardenPlanId(activity.getGardenPlanId())).thenReturn(List.of(activity));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        List<ActivityDTO> result = activityService.findByGardenPlanId(activity.getGardenPlanId());

        assertThat(result).hasSize(1);
    }

    @Test
    void findByGardenPlanId_withPageable_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(activityRepository.findByGardenPlanId(activity.getGardenPlanId(), pageable))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Page<ActivityDTO> result = activityService.findByGardenPlanId(activity.getGardenPlanId(), pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserIdAndActivityType_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(activityRepository.findByUserIdAndActivityType(activity.getUserId(), "WATERING", pageable))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Page<ActivityDTO> result =
                activityService.findByUserIdAndActivityType(activity.getUserId(), "WATERING", pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByGardenPlanIdAndActivityType_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(activityRepository.findByGardenPlanIdAndActivityType(activity.getGardenPlanId(), "WATERING", pageable))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityMapper.toDto(activity)).thenReturn(dto);

        Page<ActivityDTO> result =
                activityService.findByGardenPlanIdAndActivityType(activity.getGardenPlanId(), "WATERING", pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        when(activityMapper.toEntity(any())).thenReturn(activity);
        when(activityRepository.save(activity)).thenReturn(activity);
        when(activityMapper.toDto(activity)).thenReturn(dto);

        ActivityDTO created = activityService.create(dto);

        assertThat(created).isNotNull();
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void update_shouldModifyAndReturnDTO_whenActivityExists() {
        UUID id = activity.getId();
        when(activityRepository.findById(id)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any())).thenReturn(activity);
        when(activityMapper.toDto(any())).thenReturn(dto);

        Optional<ActivityDTO> updated = activityService.update(id, dto);

        assertThat(updated).isPresent();
        verify(activityRepository).save(any());
    }

    @Test
    void update_shouldReturnEmpty_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(activityRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ActivityDTO> updated = activityService.update(id, dto);

        assertThat(updated).isEmpty();
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();
        activityService.delete(id);
        verify(activityRepository).deleteById(id);
    }
}
