package com.utilitybilling.tariffservice.service;

import org.springframework.stereotype.Service;

import com.utilitybilling.tariffservice.dto.CreateTariffRequest;
import com.utilitybilling.tariffservice.dto.TariffResponse;
import com.utilitybilling.tariffservice.dto.UpdateTariffRequest;
import com.utilitybilling.tariffservice.exception.BusinessException;
import com.utilitybilling.tariffservice.exception.NotFoundException;
import com.utilitybilling.tariffservice.model.Tariff;
import com.utilitybilling.tariffservice.model.UtilityType;
import com.utilitybilling.tariffservice.repository.TariffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TariffService{

    private final TariffRepository repo;

    public void create(CreateTariffRequest r){
        repo.findByUtilityTypeAndActiveTrue(r.getUtilityType())
                .ifPresent(t->{
                    throw new BusinessException("Active tariff already exists for "+r.getUtilityType());
                });

        repo.save(Tariff.builder()
                .utilityType(r.getUtilityType())
                .slabs(r.getSlabs())
                .fixedCharge(r.getFixedCharge())
                .taxPercentage(r.getTaxPercentage())
                .effectiveFrom(r.getEffectiveFrom())
                .active(true)
                .build());
    }

    public TariffResponse getActive(UtilityType type){
        Tariff t=repo.findByUtilityTypeAndActiveTrue(type)
                .orElseThrow(()->new NotFoundException("No active tariff found"));

        return map(t);
    }

    public void deactivate(String id){
        Tariff t=repo.findById(id)
                .orElseThrow(()->new NotFoundException("Tariff not found"));
        t.setActive(false);
        repo.save(t);
    }

    private TariffResponse map(Tariff t){
        return TariffResponse.builder()
                .id(t.getId())
                .utilityType(t.getUtilityType())
                .slabs(t.getSlabs())
                .fixedCharge(t.getFixedCharge())
                .taxPercentage(t.getTaxPercentage())
                .active(t.isActive())
                .effectiveFrom(t.getEffectiveFrom())
                .build();
    }
    
    public void update(String tariffId,UpdateTariffRequest r){
        Tariff existing=repo.findById(tariffId)
                .orElseThrow(()->new NotFoundException("Tariff not found"));

        if(!existing.isActive())
            throw new BusinessException("Cannot update inactive tariff");

        existing.setActive(false);
        repo.save(existing);

        repo.save(Tariff.builder()
                .utilityType(existing.getUtilityType())
                .slabs(r.getSlabs())
                .fixedCharge(r.getFixedCharge())
                .taxPercentage(r.getTaxPercentage())
                .effectiveFrom(r.getEffectiveFrom())
                .active(true)
                .build());
    }
}
