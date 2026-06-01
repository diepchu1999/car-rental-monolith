package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

/**
 * Classpath path tới các file SQL của RIÊNG module customer. Mỗi module tự giữ
 * hằng số path của mình (package-private) — không gom vào 1 class trung tâm
 * chứa SQL của mọi module, để module độc lập và đổi SQL không lan ra ngoài.
 */
final class CustomerSqlPaths {

    private CustomerSqlPaths() {}

    private static final String BASE = "sql/customer/";

    static final String NEXT_HOST_CODE = BASE + "next_host_code.sql";
    static final String NEXT_KYC_CODE = BASE + "next_kyc_code.sql";
    static final String UPDATE_HOST_STATUS = BASE + "update_host_status.sql";
    static final String UPDATE_CUSTOMER_BASICS = BASE + "update_customer_basics.sql";
    static final String APPROVE_KYC = BASE + "approve_kyc.sql";
    static final String REJECT_KYC = BASE + "reject_kyc.sql";
    static final String INSERT_CUSTOMER = BASE + "insert_customer.sql";
    static final String INSERT_CUSTOMER_ROLE = BASE + "insert_customer_role.sql";
    static final String INSERT_HOST_PROFILE = BASE + "insert_host_profile.sql";
    static final String INSERT_KYC_PROFILE = BASE + "insert_kyc_profile.sql";
    static final String INSERT_KYC_DOCUMENT = BASE + "insert_kyc_document.sql";
    static final String INSERT_ADDRESS = BASE + "insert_address.sql";

    // Search (CustomerSearchAdapter)
    static final String SEARCH_CUSTOMERS_DATA = BASE + "search_customers_data.sql";
    static final String SEARCH_CUSTOMERS_COUNT = BASE + "search_customers_count.sql";

    // Stats (CustomerStatsAdapter)
    static final String CUSTOMER_STATS = BASE + "customer_stats.sql";

    // Load (CustomerLoadAdapter)
    static final String IS_ACTIVE_CUSTOMER = BASE + "is_active_customer.sql";
    static final String IS_ACTIVE_HOST = BASE + "is_active_host.sql";
    static final String EXISTS_CUSTOMER = BASE + "exists_customer.sql";
    static final String LOAD_CUSTOMER = BASE + "load_customer.sql";
    static final String LOAD_CUSTOMER_ROLES = BASE + "load_customer_roles.sql";
    static final String LOAD_HOST_PROFILE = BASE + "load_host_profile.sql";
    static final String LOAD_KYC_LIST = BASE + "load_kyc_list.sql";
    static final String LOAD_KYC_BY_ID = BASE + "load_kyc_by_id.sql";
    static final String LOAD_KYC_DOCUMENTS = BASE + "load_kyc_documents.sql";
    static final String LOAD_ADDRESSES = BASE + "load_addresses.sql";
    static final String LOAD_CUSTOMER_ACTIVITY = BASE + "load_customer_activity.sql";

    // Paged list (CustomerPageAdapter) — composed dynamically với buildWhere
    static final String PAGE_CUSTOMERS_JOINS = BASE + "page_customers_joins.sql";
    static final String KYC_AGGREGATE_CASE = BASE + "kyc_aggregate_case.sql";
    static final String PAGE_CUSTOMERS_SELECT_HEAD = BASE + "page_customers_select_head.sql";
    static final String PAGE_CUSTOMERS_SELECT_TAIL = BASE + "page_customers_select_tail.sql";
    static final String PAGE_CUSTOMERS_Q_FILTER = BASE + "page_customers_q_filter.sql";
}
