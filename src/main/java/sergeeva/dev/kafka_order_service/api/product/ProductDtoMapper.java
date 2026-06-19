package sergeeva.dev.kafka_order_service.api.product;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProductDtoMapper {

    ProductEntity toEntity(ProductDto productDto);

    ProductDto toProductDto(ProductEntity entity);
}
