package org.gamein.marketservergamein2022.infrastructure.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.BaseProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.TimeResultDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.*;
import org.gamein.marketservergamein2022.core.exception.*;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineStatus;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;
import org.gamein.marketservergamein2022.infrastructure.repository.LogRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.FactoryLineRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.RequirementRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.TeamResearchRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.RegionDistanceRepository;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.*;


@Service
public class ManufactureServiceHandler {
    private final ProductRepository productRepository;
    private final FactoryLineRepository factoryLineRepository;
    private final RequirementRepository requirementRepository;
    private final TeamRepository teamRepository;
    private final StorageProductRepository storageProductRepository;
    private final TeamResearchRepository teamResearchRepository;
    private final LogRepository logRepository;
    private final TimeRepository timeRepository;

    private final RegionDistanceRepository regionDistanceRepository;

    public ManufactureServiceHandler(ProductRepository productRepository, FactoryLineRepository factoryLineRepository,
                                     RequirementRepository requirementRepository, TeamRepository teamRepository,
                                     StorageProductRepository storageProductRepository,
                                     TeamResearchRepository teamResearchRepository, LogRepository logRepository, TimeRepository timeRepository, RegionDistanceRepository regionDistanceRepository) {
        this.productRepository = productRepository;
        this.factoryLineRepository = factoryLineRepository;
        this.requirementRepository = requirementRepository;
        this.teamRepository = teamRepository;
        this.storageProductRepository = storageProductRepository;
        this.teamResearchRepository = teamResearchRepository;
        this.logRepository = logRepository;
        this.timeRepository = timeRepository;
        this.regionDistanceRepository = regionDistanceRepository;
    }

    public List<ProductGroupDTO> getGroups(LineType lineType) {
        Stream<ProductGroup> groups;
        if (lineType == LineType.RECYCLE) {
            groups = Arrays.stream(ProductGroup.values()).limit(1);
        } else if (lineType == LineType.PRODUCTION) {
            groups = Arrays.stream(ProductGroup.values()).skip(1).limit(7);
        } else {
            groups = Arrays.stream(ProductGroup.values()).skip(7);
        }
        Time time = timeRepository.findById(1L).get();
        groups = groups.filter(g -> productRepository.existsByGroupAndAvailableDayLessThanEqual(g,
                TimeUtil.getTime(time).getDay()));
        return groups.map(
                productGroup -> new ProductGroupDTO(
                        productRepository.findAllByGroup(productGroup).stream().map(Product::toDTO).collect(Collectors.toList()),
                        productGroup
                )
        ).toList();
    }

    public FactoryLineDTO initFactoryLine(Team team, ProductGroup group, Long lineId)
            throws NotFoundException, UnauthorizedException, BadRequestException {
        FactoryLine factoryLine = validateTeamAndLine(team, lineId);
        if (!factoryLine.getStatus().equals(LineStatus.NOT_INITIAL))
            throw new BadRequestException("خط تولید شما قبلا راه اندازی شده است.");

        if ((Arrays.stream(ProductGroup.values()).toList().subList(0, 7).contains(group) &&
                factoryLine.getType().equals(LineType.ASSEMBLY)) ||
                (Arrays.stream(ProductGroup.values()).skip(7).toList().contains(group) &&
                        factoryLine.getType().equals(LineType.PRODUCTION))) {
            throw new BadRequestException("نوع نامعتبر برای این خط تولید!");
        }

        factoryLine.setGroup(group);
        factoryLine.setStatus(LineStatus.OFF);
        factoryLine.setInitiationDate(LocalDateTime.now(ZoneOffset.UTC));
        factoryLineRepository.save(factoryLine);

        return factoryLine.toDTO();
    }

    public List<CreatingRequirementsDTO> getAvailableProducts(Team team, Long lineId)
            throws UnauthorizedException, NotFoundException, BadRequestException {
        FactoryLine factoryLine = validateTeamAndLine(team, lineId);
        if (factoryLine.getStatus() == LineStatus.NOT_INITIAL) {
            throw new BadRequestException("خط تولید راه اندازی نشده است!");
        }
        Time time = timeRepository.findById(1L).get();
        TimeResultDTO resultDTO = TimeUtil.getTime(time);
        return productRepository.findAllByGroupAndEraBefore(factoryLine.getGroup(), (byte) (resultDTO.getEra() + 1)).stream()
                .map(product -> getCreatingProductRequirements(product, team, time)).collect(Collectors.toList());
    }

    private ValidateNewProcessResult validateStartNewProcess(int count, Team team, Long lineId, Long productId, Time time)
            throws BadRequestException, LineInProgressException, NotFoundException, UnauthorizedException, NotEnoughMoneyException {
        if (count <= 0)
            throw new BadRequestException("تعداد باید بیشتر از صفر باشد");

        FactoryLine factoryLine = validateTeamAndLine(team, lineId);

        if (factoryLine.getStatus().equals(LineStatus.IN_PROGRESS))
            throw new LineInProgressException("خط تولید در حال کار می باشد.");

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty())
            throw new NotFoundException("محصول مورد نظر یافت نشد!");

        if (!(getAvailableProducts(team, lineId).stream()
                .map(req -> req.getProduct().getId()).toList().contains(productId)))
            throw new BadRequestException("محصول در این خط قابل تولید نیست!");
        Product product = productOptional.get();
        if (product.getEra() > TimeUtil.getTime(time).getEra())
            throw new BadRequestException("محصول هنوز قابل تولید نیست!");

        if (team.getBalance() < ((long) count * product.getVariableCost()) + product.getFixedCost())
            throw new NotEnoughMoneyException("هزینه‌ی کافی برای این تولید را ندارید");

        if (product.getRAndD() != null && teamResearchRepository.findByTeam_IdAndSubject_IdAndEndTimeBefore(team.getId(),
                product.getRAndD().getId(), LocalDateTime.now(ZoneOffset.UTC)) == null)
            throw new BadRequestException("شما تحقیق و توسعه‌ی لازم برای این محصول را انجام نداده‌اید!");
        return new ValidateNewProcessResult(factoryLine, product);
    }

    public FactoryLineDTO startNewProcess(Team team, Long lineId, Long productId, int count)
            throws NotFoundException, UnauthorizedException, NotEnoughMaterial, LineInProgressException, BadRequestException, NotEnoughMoneyException {
        Time time = timeRepository.findById(1L).get();
        ValidateNewProcessResult result = validateStartNewProcess(count, team, lineId, productId, time);
        FactoryLine factoryLine = result.getFactoryLine();
        Product product = result.getProduct();


        List<Requirement> requirements = requirementRepository.findRequirementsByProductId(productId);
        if (factoryLine.getType() == LineType.RECYCLE) {
            return startRecycleProcess(team, factoryLine, product, count, requirements);
        }


        Long allUsedVolume = 0L;
        for (Requirement requirement : requirements) {
            allUsedVolume += (long) requirement.getRequirement().getUnitVolume() * count * requirement.getCount();
            Optional<StorageProduct> optional = getSPFromProduct(team, requirement.getRequirement());
            if (optional.isEmpty())
                throw new NotEnoughMaterial(requirement.getRequirement().getName() + " به مقدار کافی وجود ندارد.");
            StorageProduct sp = optional.get();
            if (sp.getInStorageAmount() - sp.getBlockedAmount() < requirement.getCount() * count) {
                throw new NotEnoughMaterial(requirement.getRequirement().getName() + " به مقدار کافی وجود ندارد.");
            }
        }

        if ((long) product.getUnitVolume() * count - allUsedVolume > calculateAvailableSpace(team))
            throw new BadRequestException("فضای انبار برای این تولید کافی نیست.");
        List<StorageProduct> sps = new ArrayList<>();
        for (Requirement requirement : requirements) {
            StorageProduct sp = getSPFromProduct(team, requirement.getRequirement()).get();
            removeProductFromStorage(sp, requirement.getCount() * count);
            if (sp.getSellableAmount() > sp.getInStorageAmount())
                sp.setSellableAmount(sp.getInStorageAmount());
            sps.add(sp);
        }
        StorageProduct sp = TeamUtil.getOrCreateSPFromProduct(team, product);
        TeamUtil.addProductToManufacturing(sp, count);
        sps.add(sp);
        team.setBalance(team.getBalance() - ((long) count * product.getVariableCost() + product.getFixedCost()));

        factoryLine.setStatus(LineStatus.IN_PROGRESS);
        factoryLine.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
        long duration = (long) (((double) count / product.getProductionRate()) * 8 + 1);
        factoryLine.setEndTime(factoryLine.getStartTime().plusSeconds(duration));
        factoryLine.setCount(count);
        factoryLine.setProduct(product);
        storageProductRepository.saveAll(sps);
        factoryLine = factoryLineRepository.save(factoryLine);
        teamRepository.save(team);
        return factoryLine.toDTO();

    }

    private FactoryLineDTO startRecycleProcess(Team team, FactoryLine line, Product product, int count, List<Requirement> requirements)
            throws BadRequestException {
        Requirement requirement = requirements.get(0);
        Optional<StorageProduct> optionalRecycleProduct = getSPFromProduct(team, requirement.getRequirement());
        if (optionalRecycleProduct.isEmpty() || optionalRecycleProduct.get().getInStorageAmount() < count / requirement.getCount()) {
            throw new BadRequestException("شما به میزان کافی " + requirement.getRequirement().getName() + " ندارید!");
        }
        if (count % requirement.getCount() != 0) {
            throw new BadRequestException("تعداد مواد بازیافتی باید مضربی از " + requirement.getCount() + " باشد!");
        }
        if (calculateAvailableSpace(team) < count * product.getUnitVolume()) {
            throw new BadRequestException("انبار شما فضای کافی ندارد!");
        }

        StorageProduct recycleProduct = optionalRecycleProduct.get();

        removeProductFromStorage(recycleProduct, count / requirement.getCount());

        StorageProduct sp = getOrCreateSPFromProduct(team, product);
        sp.setManufacturingAmount(count);
        storageProductRepository.save(sp);
        storageProductRepository.save(recycleProduct);


        team.setBalance(team.getBalance() - ((long) (count / requirement.getCount()) * product.getVariableCost() + product.getFixedCost()));
        team.getStorageProducts().add(sp);
        teamRepository.save(team);
        line.setStatus(LineStatus.IN_PROGRESS);
        line.setStartTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        long duration = (count / product.getProductionRate()) * 8 + 1;
        line.setEndTime(line.getStartTime().plusSeconds(duration));
        line.setCount(count);
        line.setProduct(product);

        return factoryLineRepository.save(line).toDTO();
    }

    public FactoryLineDTO cancelProcess(Team team, Long lineId)
            throws UnauthorizedException, NotFoundException, BadRequestException {
        Time time = timeRepository.findById(1L).get();
        FactoryLine factoryLine = validateTeamAndLine(team, lineId);
        if (factoryLine.getStatus() != LineStatus.IN_PROGRESS) {
            throw new BadRequestException("این خط در حال کار نیست!");
        }

        int count = factoryLine.getCount();
        List<Requirement> requirements = requirementRepository.findRequirementsByProductId(factoryLine.getProduct().getId());

        Long neededVolume = 0L;

        for (Requirement requirement : requirements) {
            neededVolume += (factoryLine.getType() == LineType.RECYCLE ? count / requirement.getCount() :
                    (long) requirement.getCount() * count) * requirement.getRequirement().getUnitVolume();
        }
        neededVolume -= (long) factoryLine.getProduct().getUnitVolume() * count;

        if (calculateAvailableSpace(team) < neededVolume)
            throw new BadRequestException("شما فضای کافی برای بازگشت مواد اولیه به انبار را ندارید.");
        List<StorageProduct> sps = new ArrayList<>();
        for (Requirement requirement : requirements) {
            StorageProduct sp = getSPFromProduct(team, requirement.getRequirement()).get();
            addProductToStorage(sp,
                    factoryLine.getType() == LineType.RECYCLE ? count / requirement.getCount() : requirement.getCount() * count);
            sps.add(sp);
        }
        StorageProduct sp = TeamUtil.getSPFromProduct(team, factoryLine.getProduct()).get();
        removeProductFromManufacturing(sp, count);
        sps.add(sp);
        team.setBalance(team.getBalance() + (long) factoryLine.getProduct().getVariableCost() * (
                factoryLine.getType() == LineType.RECYCLE ? count / requirements.get(0).getCount() : count));
        teamRepository.save(team);

        factoryLine.setStatus(LineStatus.OFF);
        factoryLine.setCount(0);
        factoryLine.setStartTime(null);
        factoryLine.setEndTime(null);

        storageProductRepository.saveAll(sps);
        return factoryLineRepository.save(factoryLine).toDTO();
    }

    public List<FactoryLineDTO> getTeamLines(Team team) {
        return factoryLineRepository.findAllByTeamId(team.getId())
                .stream().map(FactoryLine::toDTO).collect(Collectors.toList());
    }

    public FactoryLineDTO collectLine(Team team, Long lineId) throws UnauthorizedException, NotFoundException, RemainignTimeException, BadRequestException {
        FactoryLine factoryLine = validateTeamAndLine(team, lineId);

        if (factoryLine.getStatus().equals(LineStatus.OFF))
            throw new BadRequestException("خط تولید خالی می باشد.");


        if (LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).isBefore(factoryLine.getEndTime()))
            throw new RemainignTimeException("اکنون زمان تحویل نمی باشد.");

        Optional<StorageProduct> optional = TeamUtil.getSPFromProduct(team, factoryLine.getProduct());
        if (optional.isEmpty())
            throw new BadRequestException("خط تولید خالی می باشد.");

        StorageProduct sp = optional.get();
        TeamUtil.removeProductFromManufacturing(sp, factoryLine.getCount());
        TeamUtil.addProductToSellable(sp, factoryLine.getCount());
        TeamUtil.addProductToStorage(sp, factoryLine.getCount());

        storageProductRepository.save(sp);

        //log

        Time time = timeRepository.findById(1L).get();
        TimeResultDTO resultDTO = TimeUtil.getTime(time);
        saveLog(team, factoryLine, sp.getProduct(), resultDTO);

        factoryLine.setStatus(LineStatus.OFF);
        factoryLine.setCount(0);
        factoryLine.setStartTime(null);
        factoryLine.setEndTime(null);

        return factoryLineRepository.save(factoryLine).toDTO();
    }

    private void saveLog(Team team, FactoryLine factoryLine, Product product, TimeResultDTO timeResultDTO) {
        //log
        Log log = new Log();

        LogType logType = LogType.PRODUCTION;
        if (factoryLine.getType().equals(LineType.ASSEMBLY)) {
            logType = LogType.ASSEMBLY;
        } else if (factoryLine.getType().equals(LineType.RECYCLE)) {
            logType = LogType.RECYCLE;
        }
        log.setType(logType);
        Long logPrice =
                ((long) product.getVariableCost() * (factoryLine.getType() != LineType.RECYCLE ?
                        factoryLine.getCount() :
                        factoryLine.getCount() / requirementRepository.findRequirementsByProductId(factoryLine.getProduct().getId()).get(0).getCount()))
                        + product.getFixedCost();
        log.setTotalCost(logPrice);
        log.setTeam(team);
        log.setProductCount(Long.valueOf(factoryLine.getCount()));
        log.setProduct(product);
        log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                Math.toIntExact(timeResultDTO.getMonth()),
                Math.toIntExact(timeResultDTO.getDay()),
                12,
                23));
        ;
        logRepository.save(log);
    }

    private FactoryLine validateTeamAndLine(Team team, Long lineId) throws NotFoundException, UnauthorizedException {
        List<Building> buildings = team.getBuildings();
        for (Building building : buildings) {
            List<FactoryLine> factoryLines = building.getLines();
            for (FactoryLine factoryLine : factoryLines) {
                if (factoryLine.getId() == lineId) {
                    return factoryLine;
                }
            }
        }
        throw new NotFoundException("خط تولید شما یافت نشد.");
    }


    public CreatingRequirementsDTO getCreatingProductRequirements(Product product, Team team, Time time) {
        List<RequirementDTO> requirementDTOS = new ArrayList<>();
        List<Requirement> requirements = requirementRepository.findRequirementsByProductId(product.getId());
        for (Requirement requirement : requirements) {
            Product requirementProduct = productRepository.findProductById(requirement.getRequirement().getId());
            BaseProductDTO dto;

            if (requirementProduct.getLevel() <= 0) {
                int distance = regionDistanceRepository.minDistance(requirementProduct.getRegions(), team.getRegion());

                dto = requirementProduct.rawMaterialDTO(distance);
            } else {
                dto = requirementProduct.toDTO();
            }


            List<StorageProduct> teamStorage = team.getStorageProducts();
            long inStorageAmount = 0L;

            for (StorageProduct storageProduct : teamStorage) {
                if (storageProduct.getProduct().getId().equals(requirementProduct.getId())) {
                    inStorageAmount = storageProduct.getInStorageAmount() - storageProduct.getBlockedAmount();
                    break;
                }
            }

            RequirementDTO r = new RequirementDTO(dto, inStorageAmount, requirement.getCount());

            requirementDTOS.add(r);
        }
        return new CreatingRequirementsDTO(
                requirementDTOS, product.getFixedCost(),product.getVariableCost(), team.getBalance(), product.toDTO(),
                product.getRAndD() == null ||
                        teamResearchRepository.findByTeam_IdAndSubject_IdAndEndTimeBefore(team.getId(),
                                product.getRAndD().getId(), LocalDateTime.now(ZoneOffset.UTC)) != null
        );
    }
}
