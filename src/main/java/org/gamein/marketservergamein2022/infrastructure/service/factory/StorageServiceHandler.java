package org.gamein.marketservergamein2022.infrastructure.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.StorageInfoDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.StorageProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.factory.StorageService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.RequirementRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@EnableScheduling
@Configuration
public class StorageServiceHandler implements StorageService {


    @Value("${live.data.url}")
    private String liveUrl;

    private final RequirementRepository requirementRepository;
    private final ProductRepository productRepository;
    private final ShippingRepository shippingRepository;
    private final StorageProductRepository storageProductRepository;
    private final TeamRepository teamRepository;

    private final TimeRepository timeRepository;


    public StorageServiceHandler(RequirementRepository requirementRepository, ProductRepository productRepository,
                                 ShippingRepository shippingRepository, TeamRepository teamRepository,
                                 StorageProductRepository storageProductRepository, TimeRepository timeRepository) {
        this.requirementRepository = requirementRepository;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.shippingRepository = shippingRepository;
        this.storageProductRepository = storageProductRepository;
        this.timeRepository = timeRepository;
    }

    @Override
    public StorageInfoDTO getStorageInfo(Team team) {
        Time time = timeRepository.findById(1L).get();
        List<StorageProductDTO> products = team.getStorageProducts().stream()
                .filter(sp -> sp.getInStorageAmount() != 0 || sp.getManufacturingAmount() != 0)
                .map(StorageProduct::toDTO).collect(Collectors.toList());
        int total = TeamUtil.calculateStorageSpace(team);
        int inStorage = TeamUtil.calculateUsedSpace(team);
        int manufacturing = TeamUtil.calculateManufacturing(team);
        int blocked = TeamUtil.calculateBlockedAmount(team);
        int freeSpace = TeamUtil.calculateAvailableSpace(team);
        return new StorageInfoDTO(products,
                (((double) inStorage - blocked) / total) * 100,
                (((double) manufacturing) / total) * 100,
                (((double) freeSpace) / total) * 100,
                (((double) blocked) / total) * 100,
                total,
                team.getIsStorageUpgraded(),
                time.getUpgradeStorageCost()
        );
    }



    @Override
    public List<ShippingDTO> getStorageQueue(Team team) {
        Time time = timeRepository.findById(1L).get();
        return shippingRepository.findAllByTeam_IdAndStatus(team.getId(), ShippingStatus.IN_QUEUE).stream()
                .map(shipping -> shipping.toDTO(time)).collect(Collectors.toList());
    }

    @Override
    public StorageInfoDTO collectFromQueue(Team team, Long shippingId)
            throws NotFoundException, BadRequestException {
        Time time = timeRepository.findById(1L).get();
        Shipping shipping = getShippingFromId(shippingId);
        if (shipping.getStatus() != ShippingStatus.IN_QUEUE) {
            throw new NotFoundException("مورد درخواست‌شده در صف انبار نیست!");
        }
        if (shipping.getArrivalTime().plusSeconds(60).isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BadRequestException("مهلت جمع‌آوری این سفارش به پایان رسیده است!");
        }
        if ((long)shipping.getProduct().getUnitVolume() * shipping.getAmount() > TeamUtil.calculateAvailableSpace(team,time)) {
            throw new BadRequestException("انبار شما فضای کافی ندارد!");
        }
        StorageProduct sp = TeamUtil.addProductToStorage(team, shipping.getProduct(), shipping.getAmount(),
                storageProductRepository, teamRepository);
        storageProductRepository.save(sp);
        sp = TeamUtil.removeProductFromRoute(team, shipping.getProduct(), shipping.getAmount(), storageProductRepository);
        storageProductRepository.save(sp);
        shipping.setStatus(ShippingStatus.DONE);
        shippingRepository.save(shipping);

        return getStorageInfo(team);
    }

    @Override
    public StorageInfoDTO removeFromQueue(Team team, Long shippingId)
            throws NotFoundException, BadRequestException { //
        Shipping shipping = getShippingFromId(shippingId);
        if (shipping.getStatus() != ShippingStatus.IN_QUEUE) {
            throw new NotFoundException("مورد درخواست‌شده در صف انبار نیست!");
        }

        StorageProduct sp = TeamUtil.removeProductFromRoute(team, shipping.getProduct(), shipping.getAmount(), storageProductRepository);
        storageProductRepository.save(sp);
        shipping.setStatus(ShippingStatus.DONE);
        shippingRepository.save(shipping);

        team.setBalance(team.getBalance() + (int)(0.5 * shipping.getProduct().getMinPrice() * shipping.getAmount()));
        teamRepository.save(team);

        return getStorageInfo(team);
    }

    @Override
    public List<ShippingDTO> getInRouteShippings(Team team) {
        Time time = timeRepository.findById(1L).get();
        return shippingRepository.findAllByTeam_IdAndStatus(team.getId(), ShippingStatus.IN_ROUTE).stream()
                .map(shipping -> shipping.toDTO(time)).collect(Collectors.toList());
    }

    @Override
    public StorageInfoDTO removeFromStorage(Team team, Long productId, Integer quantity)
            throws BadRequestException {
        if (quantity <= 0)
            throw new BadRequestException("تعداد باید بزرگ تر از صفر باشد.");
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("محصول مورد نظر یافت نشد!");
        }
        Product product = productOptional.get();
        StorageProduct sp = TeamUtil.removeProductFromStorage(team, product, quantity, storageProductRepository);
        team.setBalance(team.getBalance() + (int) (0.5 * product.getMinPrice() * quantity));
        storageProductRepository.save(sp);
        teamRepository.save(team);
        return getStorageInfo(team);
    }

    @Override
    public void upgradeStorage(Team team) throws BadRequestException {
        Time time = timeRepository.findById(1L).get();
        if (team.getIsStorageUpgraded())
            throw new BadRequestException("انبار شما قبلا ارتقا یافته است.");
        if (team.getBalance() < time.getUpgradeStorageCost())
            throw new BadRequestException("سرمایه ی شما کافی نمی باشد.");
        team.setIsStorageUpgraded(true);
        team.setBalance(team.getBalance() - time.getUpgradeStorageCost());
        teamRepository.save(team);
    }

    private Shipping getShippingFromId(Long shippingId) throws NotFoundException {
        Optional<Shipping> shippingOptional = shippingRepository.findById(shippingId);
        if (shippingOptional.isEmpty()) {
            throw new NotFoundException("ترابری مورد نظر یافت نشد!");
        }
        return shippingOptional.get();
    }
}
