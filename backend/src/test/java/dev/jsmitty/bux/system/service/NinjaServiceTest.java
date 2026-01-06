package dev.jsmitty.bux.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class NinjaServiceTest {

    @Mock NinjaRepository ninjaRepository;
    @Mock FacilityRepository facilityRepository;
    @Mock LedgerService ledgerService;
    @Mock PointCalculator pointCalculator;
    @Mock CodeNinjasApiClient codeNinjasApiClient;

    @InjectMocks NinjaService ninjaService;

    @Test
    void getNinjas_capsPageSizeAndAppliesDefaultSort() {
        UUID facilityId = UUID.randomUUID();

        Pageable requested = PageRequest.of(0, 500); // too large, no sort

        // If Ninja is hard to construct, you can return an empty page for this test
        Page<Ninja> repoPage = new PageImpl<>(List.of(), requested, 0);

        when(ninjaRepository.findByFacilityId(eq(facilityId), any(Pageable.class)))
                .thenReturn(repoPage);

        ninjaService.getNinjas(facilityId, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ninjaRepository).findByFacilityId(eq(facilityId), captor.capture());

        Pageable effective = captor.getValue();
        assertThat(effective.getPageSize()).isEqualTo(100);
        assertThat(effective.getSort().isSorted()).isTrue();
        assertThat(effective.getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }
}
