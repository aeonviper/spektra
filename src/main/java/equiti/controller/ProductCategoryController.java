package equiti.controller;

import com.google.inject.Inject;

import equiti.model.ProductCategory;
import equiti.service.ProductCategoryService;
import omega.service.TransactionService;

public class ProductCategoryController extends ModelController<ProductCategory> {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected ProductCategoryService productCategoryService;

	public ProductCategory find(Long entityId, Long companyId) {
		return productCategoryService.find(entityId, companyId);
	}

	public ProductCategory add(ProductCategory entity) {
		productCategoryService.save(entity);
		return entity;
	}

	public ProductCategory update(ProductCategory entity) {
		ProductCategory productCategory = productCategoryService.find(entity.getId(), entity.getCompanyId());
		if (productCategory != null) {
			productCategory.setName(entity.getName());
			productCategory.setProductCategoryId(entity.getProductCategoryId());
			productCategory.setDescription(entity.getDescription());
			productCategoryService.save(productCategory);
		}
		return productCategory;
	}

	public ProductCategory delete(Long entityId, Long companyId) {
		productCategoryService.delete(entityId, companyId);
		return null;
	}
}
