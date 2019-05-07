package equiti.controller;

import com.google.inject.Inject;

import equiti.model.Product;
import equiti.service.ProductService;
import omega.service.TransactionService;

public class ProductController extends ModelController<Product> {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected ProductService productService;

	public Product find(Long entityId, Long companyId) {
		return productService.find(entityId, companyId);
	}

	public Product add(Product entity) {
		productService.save(entity);
		return entity;
	}

	public Product update(Product entity) {
		Product product = productService.find(entity.getId(), entity.getCompanyId());
		if (product != null) {
			product.setName(entity.getName());
			product.setProductCategoryId(entity.getProductCategoryId());
			product.setCode(entity.getCode());
			product.setUnit(entity.getUnit());
			product.setBuyPrice(entity.getBuyPrice());
			product.setSellPrice(entity.getSellPrice());
			product.setDescription(entity.getDescription());
			product.setInformation(entity.getInformation());
			productService.save(product);
		}
		return product;
	}

	public Product delete(Long entityId, Long companyId) {
		productService.delete(entityId, companyId);
		return null;
	}
}
