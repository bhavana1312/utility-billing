//package com.utilitybilling.tariffservice.service;
//
//import com.utilitybilling.tariffservice.dto.*;
//import com.utilitybilling.tariffservice.exception.*;
//import com.utilitybilling.tariffservice.model.*;
//import com.utilitybilling.tariffservice.repository.TariffRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TariffServiceTest {
//
//    @Mock
//    private TariffRepository repo;
//
//    @InjectMocks
//    private TariffService service;
//
//    @Test
//    void create_success() {
//        CreateTariffRequest r=new CreateTariffRequest();
//        r.setUtilityType(UtilityType.ELECTRICITY);
//        r.setSlabs(List.of());
//        r.setFixedCharge(50);
//        r.setTaxPercentage(18);
//        r.setEffectiveFrom(LocalDate.now());
//
//        when(repo.findByUtilityTypeAndActiveTrue(UtilityType.ELECTRICITY))
//                .thenReturn(Optional.empty());
//
//        service.create(r);
//
//        verify(repo,times(1)).save(any(Tariff.class));
//    }
//
//    @Test
//    void create_activeTariffExists_throwsException() {
//        when(repo.findByUtilityTypeAndActiveTrue(UtilityType.ELECTRICITY))
//                .thenReturn(Optional.of(new Tariff()));
//
//        CreateTariffRequest r=new CreateTariffRequest();
//        r.setUtilityType(UtilityType.ELECTRICITY);
//
//        assertThrows(BusinessException.class,()->service.create(r));
//    }
//
//    @Test
//    void getActive_success() {
//        Tariff t=Tariff.builder()
//                .id("1")
//                .utilityType(UtilityType.WATER)
//                .active(true)
//                .build();
//
//        when(repo.findByUtilityTypeAndActiveTrue(UtilityType.WATER))
//                .thenReturn(Optional.of(t));
//
//        TariffResponse res=service.getActive(UtilityType.WATER);
//
//        assertEquals("1",res.getId());
//    }
//
//    @Test
//    void getActive_notFound() {
//        when(repo.findByUtilityTypeAndActiveTrue(UtilityType.GAS))
//                .thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//                ()->service.getActive(UtilityType.GAS));
//    }
//
//    @Test
//    void deactivate_success() {
//        Tariff t=Tariff.builder().id("1").active(true).build();
//        when(repo.findById("1")).thenReturn(Optional.of(t));
//
//        service.deactivate("1");
//
//        assertFalse(t.isActive());
//        verify(repo).save(t);
//    }
//
//    @Test
//    void deactivate_notFound() {
//        when(repo.findById("x")).thenReturn(Optional.empty());
//        assertThrows(NotFoundException.class,()->service.deactivate("x"));
//    }
//
//    @Test
//    void update_success() {
//        Tariff existing=Tariff.builder()
//                .id("1")
//                .utilityType(UtilityType.INTERNET)
//                .active(true)
//                .build();
//
//        when(repo.findById("1")).thenReturn(Optional.of(existing));
//
//        UpdateTariffRequest r=new UpdateTariffRequest();
//        r.setSlabs(List.of());
//        r.setFixedCharge(60);
//        r.setTaxPercentage(10);
//        r.setEffectiveFrom(LocalDate.now());
//
//        service.update("1",r);
//
//        verify(repo,times(2)).save(any(Tariff.class));
//    }
//
//    @Test
//    void update_inactiveTariff_throwsException() {
//        Tariff inactive = Tariff.builder().active(false).build();
//        when(repo.findById("1")).thenReturn(Optional.of(inactive));
//
//        UpdateTariffRequest request = new UpdateTariffRequest();
//
//        assertThrows(
//            BusinessException.class,
//            () -> service.update("1", request)
//        );
//    }
//
//}
