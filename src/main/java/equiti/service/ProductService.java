package equiti.service;

import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.Product;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class ProductService extends BaseService {

	public static final Decorator<Product> toDecorator = new Decorator<Product>() {
		public Product decorate(Product entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<Product> fromDecorator = new Decorator<Product>() {
		public Product decorate(Product entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(Product product) {
		toDecorator.decorate(product);
		if (product.getId() != null) {
			return update(product);
		} else {
			return insert(product);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(Product product) {
		product.setId(sequence("entitySequence"));
		return write( //
		"insert into product (id, companyId, name, data, productCategoryId, code, unit, buyPrice, sellPrice, description, information) values (?, ?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?, ?)", //
		product.getId(), product.getCompanyId(), product.getName(), product.getData(), //
		product.getProductCategoryId(), product.getCode(), product.getUnit(), product.getBuyPrice(), product.getSellPrice(), //
		product.getDescription(), product.getInformation() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(Product product) {
		return write( //
		"update product set name = ?, data = to_json(?::json), productCategoryId = ?, code = ?, unit = ?, buyPrice = ?, sellPrice = ?, description = ?, information = ? where id = ? and companyId = ?", //
		product.getName(), product.getData(), //
		product.getProductCategoryId(), product.getCode(), product.getUnit(), product.getBuyPrice(), product.getSellPrice(), //
		product.getDescription(), product.getInformation(), //
		product.getId(), product.getCompanyId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id, Long companyId) {
		return write( //
		"delete from product where id = ? and companyId = ?", //
		id, companyId //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public Product find(Long id, Long companyId) {
		return fromDecorator.decorate(find(Product.class, "select * from product where id = ? and companyId = ?", id, companyId));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<Product> list(Long companyId) {
		return fromDecorator.decorate(list(Product.class, "select * from product where companyId = ? order by id", companyId));
	}
}
