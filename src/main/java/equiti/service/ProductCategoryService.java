package equiti.service;

import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.ProductCategory;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class ProductCategoryService extends BaseService {

	public static final Decorator<ProductCategory> toDecorator = new Decorator<ProductCategory>() {
		public ProductCategory decorate(ProductCategory entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<ProductCategory> fromDecorator = new Decorator<ProductCategory>() {
		public ProductCategory decorate(ProductCategory entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(ProductCategory productCategory) {
		toDecorator.decorate(productCategory);
		if (productCategory.getId() != null) {
			return update(productCategory);
		} else {
			return insert(productCategory);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(ProductCategory productCategory) {
		productCategory.setId(sequence("entitySequence"));
		return write( //
		"insert into productCategory (id, companyId, name, data, productCategoryId, description) values (?, ?, ?, to_json(?::json), ?, ?)", //
		productCategory.getId(), productCategory.getCompanyId(), productCategory.getName(), productCategory.getData(), //
		productCategory.getProductCategoryId(), productCategory.getDescription() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(ProductCategory productCategory) {
		return write( //
		"update productCategory set name = ?, data = to_json(?::json), productCategoryId = ?, description = ? where id = ? and companyId = ?", //
		productCategory.getName(), productCategory.getData(), //
		productCategory.getProductCategoryId(), productCategory.getDescription(), //
		productCategory.getId(), productCategory.getCompanyId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id, Long companyId) {
		return write( //
		"delete from productCategory where id = ? and companyId = ?", //
		id, companyId //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public ProductCategory find(Long id, Long companyId) {
		return fromDecorator.decorate(find(ProductCategory.class, "select * from productCategory where id = ? and companyId = ?", id, companyId));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<ProductCategory> list(Long companyId) {
		return fromDecorator.decorate(list(ProductCategory.class, "select * from productCategory where companyId = ? order by id", companyId));
	}
}
