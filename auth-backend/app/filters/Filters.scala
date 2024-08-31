package filters

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.csrf.CSRFFilter
import filters.PolicyFilter

class Filters @Inject() (
  csrfFilter: CSRFFilter,
  policyFilter: PolicyFilter
) extends DefaultHttpFilters(csrfFilter, policyFilter)
