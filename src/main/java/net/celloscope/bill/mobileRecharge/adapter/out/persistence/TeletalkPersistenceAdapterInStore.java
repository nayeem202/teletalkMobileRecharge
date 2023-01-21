package net.celloscope.bill.mobileRecharge.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.bill.mobileRecharge.application.port.out.LoadTeletalkMobileRecharge;
import net.celloscope.bill.mobileRecharge.application.port.out.SaveTeletalkRechargeInStore;
import net.celloscope.bill.mobileRecharge.application.port.out.UpdateTeletalkMobileRecharge;
import net.celloscope.bill.mobileRecharge.domain.TeletalkRecharge;
import net.celloscope.bill.mobileRecharge.shared.util.ExceptionHandlerUtil;
import org.modelmapper.Provider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@PersistenceAdapter
public class TeletalkPersistenceAdapterInStore implements SaveTeletalkRechargeInStore, LoadTeletalkMobileRecharge, UpdateTeletalkMobileRecharge {

    private final TeletalkMobileRechargeRepository teletalkMobileRechargeRepository;
    private final TeletalkTransactionMapper teletalkTransactionMapper;


    @Override
    public Mono<List<TeletalkRecharge>> findByMobileNoAndAmount(String mobileNo, Double amount) {
        Flux<TeletalkMobileRechargeEntity> rechargeEntityFlux = teletalkMobileRechargeRepository.findByMobileNoAndAmount(mobileNo, amount);
        return rechargeEntityFlux.map(rechargeEntity -> teletalkTransactionMapper.mapToDomainEntity(rechargeEntity));
    }

    @Override
    public Flux<TeletalkRecharge> save(TeletalkRecharge rechargeTransaction) throws ExceptionHandlerUtil {
        Provider<TeletalkMobileRechargeEntity> teletalkMobileRechargeEntityProvider = provider -> new TeletalkMobileRechargeEntity();
        return teletalkMobileRechargeRepository.save(teletalkTransactionMapper.mapToJpaEntity(teletalkMobileRechargeEntityProvider, rechargeTransaction))
                .map(teletalkMobileRechargeEntity -> teletalkTransactionMapper.mapToDomainEntity(teletalkMobileRechargeEntity));
    }

    @Override
    public Mono<TeletalkRecharge> updateState(TeletalkRecharge rechargeTransaction) {
        Mono<TeletalkMobileRechargeEntity> rechargeEntityMono = teletalkMobileRechargeRepository.findByOriginatorConversationId(rechargeTransaction.getOriginatorConversationId())
                .flatMap(rechargeEntityProvider -> teletalkMobileRechargeRepository.save(teletalkTransactionMapper.mapToJpaEntity(rechargeEntityProvider, rechargeTransaction)));

        return rechargeEntityMono.map(rechargeEntity -> teletalkTransactionMapper.mapToDomainEntity(rechargeEntity));
    }


}
