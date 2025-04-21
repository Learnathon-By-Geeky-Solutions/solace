package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.ProfileDTO;
import dev.solace.twiggle.mapper.ProfileMapper;
import dev.solace.twiggle.model.Profile;
import dev.solace.twiggle.repository.ProfileRepository;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile1;
    private Profile profile2;
    private ProfileDTO profileDTO1;
    private ProfileDTO profileDTO2;
    private UUID profile1Uuid;
    private UUID profile2Uuid;

    @BeforeEach
    void setUp() {
        profile1Uuid = UUID.randomUUID();
        profile2Uuid = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        profile1 = new Profile();
        profile1.setId(profile1Uuid);
        profile1.setFullName("Garden User One");
        profile1.setAvatarUrl("http://example.com/avatar1.png");
        profile1.setCreatedAt(now.minusDays(1));
        profile1.setUpdatedAt(now);

        profile2 = new Profile();
        profile2.setId(profile2Uuid);
        profile2.setFullName("Garden User Two");
        profile2.setAvatarUrl("http://example.com/avatar2.png");
        profile2.setCreatedAt(now.minusHours(5));
        profile2.setUpdatedAt(now.minusHours(1));

        profileDTO1 = ProfileDTO.builder()
                .fullName(profile1.getFullName())
                .avatarUrl(profile1.getAvatarUrl())
                .createdAt(profile1.getCreatedAt())
                .updatedAt(profile1.getUpdatedAt())
                .build();

        profileDTO2 = ProfileDTO.builder()
                .fullName(profile2.getFullName())
                .avatarUrl(profile2.getAvatarUrl())
                .createdAt(profile2.getCreatedAt())
                .updatedAt(profile2.getUpdatedAt())
                .build();
    }

    @Test
    void findAll_ShouldReturnAllProfiles() {
        List<Profile> profiles = Arrays.asList(profile1, profile2);
        when(profileRepository.findAll()).thenReturn(profiles);
        when(profileMapper.toDto(profile1)).thenReturn(profileDTO1);
        when(profileMapper.toDto(profile2)).thenReturn(profileDTO2);

        List<ProfileDTO> result = profileService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(profileDTO1.getFullName(), result.get(0).getFullName());
        assertEquals(profileDTO2.getFullName(), result.get(1).getFullName());
        verify(profileRepository, times(1)).findAll();
        verify(profileMapper, times(1)).toDto(profile1);
        verify(profileMapper, times(1)).toDto(profile2);
    }

    @Test
    void findById_WithValidId_ShouldReturnOptionalProfile() {
        when(profileRepository.findById(profile1Uuid)).thenReturn(Optional.of(profile1));
        when(profileMapper.toDto(profile1)).thenReturn(profileDTO1);

        Optional<ProfileDTO> result = profileService.findById(profile1Uuid);

        assertTrue(result.isPresent());
        assertEquals(profileDTO1.getFullName(), result.get().getFullName());
        assertEquals(profileDTO1.getAvatarUrl(), result.get().getAvatarUrl());
        verify(profileRepository, times(1)).findById(profile1Uuid);
        verify(profileMapper, times(1)).toDto(profile1);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmptyOptional() {
        UUID invalidUuid = UUID.randomUUID();
        when(profileRepository.findById(invalidUuid)).thenReturn(Optional.empty());

        Optional<ProfileDTO> result = profileService.findById(invalidUuid);

        assertTrue(result.isEmpty());
        verify(profileRepository, times(1)).findById(invalidUuid);
        verify(profileMapper, never()).toDto(any());
    }

    @Test
    void create_ShouldReturnCreatedProfile() {
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("New User")
                .avatarUrl("http://new.png")
                .build();

        Profile entityToSave = new Profile();
        entityToSave.setFullName(requestDto.getFullName());
        entityToSave.setAvatarUrl(requestDto.getAvatarUrl());

        Profile savedEntity = new Profile();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFullName(requestDto.getFullName());
        savedEntity.setAvatarUrl(requestDto.getAvatarUrl());
        savedEntity.setCreatedAt(OffsetDateTime.now());
        savedEntity.setUpdatedAt(OffsetDateTime.now());

        ProfileDTO responseDto = ProfileDTO.builder()
                .fullName(savedEntity.getFullName())
                .avatarUrl(savedEntity.getAvatarUrl())
                .createdAt(savedEntity.getCreatedAt())
                .updatedAt(savedEntity.getUpdatedAt())
                .build();

        when(profileMapper.toEntity(any(ProfileDTO.class))).thenReturn(entityToSave);
        when(profileRepository.save(any(Profile.class))).thenReturn(savedEntity);
        when(profileMapper.toDto(savedEntity)).thenReturn(responseDto);

        ProfileDTO result = profileService.create(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getFullName(), result.getFullName());
        assertEquals(responseDto.getAvatarUrl(), result.getAvatarUrl());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(profileMapper, times(1)).toEntity(any(ProfileDTO.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(profileMapper, times(1)).toDto(savedEntity);
    }

    @Test
    void update_WithValidId_ShouldReturnUpdatedOptionalProfile() {
        ProfileDTO updateRequestDto = ProfileDTO.builder()
                .fullName("Updated User One")
                .avatarUrl("http://updated.png")
                .build();

        Profile existingProfile = new Profile();
        existingProfile.setId(profile1Uuid);
        existingProfile.setFullName(profile1.getFullName());
        existingProfile.setAvatarUrl(profile1.getAvatarUrl());
        existingProfile.setCreatedAt(profile1.getCreatedAt());
        existingProfile.setUpdatedAt(profile1.getUpdatedAt());

        Profile profileAfterUpdate = new Profile();
        profileAfterUpdate.setId(existingProfile.getId());
        profileAfterUpdate.setFullName(updateRequestDto.getFullName());
        profileAfterUpdate.setAvatarUrl(updateRequestDto.getAvatarUrl());
        profileAfterUpdate.setCreatedAt(existingProfile.getCreatedAt());

        Profile savedProfile = new Profile();
        savedProfile.setId(profileAfterUpdate.getId());
        savedProfile.setFullName(profileAfterUpdate.getFullName());
        savedProfile.setAvatarUrl(profileAfterUpdate.getAvatarUrl());
        savedProfile.setCreatedAt(profileAfterUpdate.getCreatedAt());
        savedProfile.setUpdatedAt(OffsetDateTime.now());

        ProfileDTO responseDto = ProfileDTO.builder()
                .fullName(savedProfile.getFullName())
                .avatarUrl(savedProfile.getAvatarUrl())
                .createdAt(savedProfile.getCreatedAt())
                .updatedAt(savedProfile.getUpdatedAt())
                .build();

        when(profileRepository.findById(profile1Uuid)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);
        when(profileMapper.toDto(savedProfile)).thenReturn(responseDto);

        Optional<ProfileDTO> result = profileService.update(profile1Uuid, updateRequestDto);

        assertTrue(result.isPresent());
        assertEquals(responseDto.getFullName(), result.get().getFullName());
        assertEquals(responseDto.getAvatarUrl(), result.get().getAvatarUrl());
        verify(profileRepository, times(1)).findById(profile1Uuid);
        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(profileMapper, times(1)).toDto(savedProfile);
    }

    @Test
    void update_WithInvalidId_ShouldReturnEmptyOptional() {
        UUID invalidUuid = UUID.randomUUID();
        ProfileDTO updateRequestDto =
                ProfileDTO.builder().fullName("Fail Update").build();
        when(profileRepository.findById(invalidUuid)).thenReturn(Optional.empty());

        Optional<ProfileDTO> result = profileService.update(invalidUuid, updateRequestDto);

        assertTrue(result.isEmpty());
        verify(profileRepository, times(1)).findById(invalidUuid);
        verify(profileRepository, never()).save(any());
        verify(profileMapper, never()).toDto(any());
    }

    @Test
    void delete_WithValidId_ShouldDeleteProfile() {
        doNothing().when(profileRepository).deleteById(profile1Uuid);

        profileService.delete(profile1Uuid);

        verify(profileRepository, times(1)).deleteById(profile1Uuid);
    }
}
