package mw.gov.health.lmis.upload;

import org.springframework.stereotype.Service;

@Service
public class OrderableDisplayCategoryService extends BaseCommunicationService {

  @Override
  protected String getUrl() {
    return "/api/orderableDisplayCategories";
  }

}
