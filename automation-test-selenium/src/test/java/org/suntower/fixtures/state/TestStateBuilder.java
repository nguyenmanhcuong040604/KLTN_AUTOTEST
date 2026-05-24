package org.suntower.fixtures.state;

import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.suntower.core.AppConfig;
import org.suntower.fixtures.auth.TestAccountResolver;
import org.suntower.fixtures.data.TestDataFactory;

public final class TestStateBuilder {
  private TestStateBuilder() {}

  public static CreatedCustomer createCustomer() {
    return createCustomer(null);
  }

  public static CreatedCustomer createCustomer(Integer staffId) {
    TestDataFactory.CustomerPayload payload = TestDataFactory.buildCustomerPayload();
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "username", payload.username(),
                    "password", payload.password(),
                    "fullName", payload.fullName(),
                    "phone", payload.phone(),
                    "email", payload.email(),
                    "staffIds", staffId == null ? java.util.List.of() : java.util.List.of(staffId)))
            .post("/api/v1/admin/customers");

    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create customer fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }

    Integer id = findCustomerIdByUsername(payload.username());
    if (id == null) {
      throw new IllegalStateException("Cannot resolve created customer id by username: " + payload.username());
    }
    return new CreatedCustomer(id, payload.username(), payload.password(), payload.fullName(), payload.email());
  }

  public static void deleteCustomer(Integer id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/customers/" + id);
    MySqlDbClient.execute("DELETE FROM invoice_detail WHERE invoice_id IN (SELECT id FROM invoice WHERE customer_id = ?)", id);
    MySqlDbClient.execute("DELETE FROM invoice WHERE customer_id = ?", id);
    MySqlDbClient.execute("DELETE FROM contract WHERE customer_id = ?", id);
    MySqlDbClient.execute("DELETE FROM sale_contract WHERE customer_id = ?", id);
    MySqlDbClient.execute("DELETE FROM assignment_customer WHERE customer_id = ?", id);
    MySqlDbClient.execute("DELETE FROM customer WHERE id = ?", id);
  }

  public record CreatedCustomer(Integer id, String username, String password, String fullName, String email) {}

  public static CreatedStaff createStaff(String role) {
    TestDataFactory.StaffPayload payload = TestDataFactory.buildStaffPayload(role);
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "username", payload.username(),
                    "password", payload.password(),
                    "fullName", payload.fullName(),
                    "phone", payload.phone(),
                    "email", payload.email(),
                    "role", payload.role()))
            .post("/api/v1/admin/staff");

    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create staff fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }

    Long id = findStaffIdByUsername(payload.username());
    if (id == null) {
      throw new IllegalStateException("Cannot resolve created staff id by username: " + payload.username());
    }
    return new CreatedStaff(id, payload.username(), payload.password(), payload.fullName(), payload.email(), payload.role());
  }

  public static void deleteStaff(Long id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/staff/" + id);
    MySqlDbClient.execute("DELETE FROM invoice_detail WHERE invoice_id IN (SELECT i.id FROM invoice i JOIN contract c ON c.id = i.contract_id WHERE c.staff_id = ?)", id);
    MySqlDbClient.execute("DELETE FROM invoice WHERE contract_id IN (SELECT id FROM contract WHERE staff_id = ?)", id);
    MySqlDbClient.execute("DELETE FROM contract WHERE staff_id = ?", id);
    MySqlDbClient.execute("DELETE FROM sale_contract WHERE staff_id = ?", id);
    MySqlDbClient.execute("DELETE FROM assignment_customer WHERE staff_id = ?", id);
    MySqlDbClient.execute("DELETE FROM assignment_building WHERE staff_id = ?", id);
    MySqlDbClient.execute("DELETE FROM staff WHERE id = ?", id);
  }

  public static boolean staffCustomerAssignmentExists(Long staffId, Integer customerId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM assignment_customer WHERE staff_id = ? AND customer_id = ?", staffId, customerId);
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static boolean staffBuildingAssignmentExists(Long staffId, Long buildingId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM assignment_building WHERE staff_id = ? AND building_id = ?", staffId, buildingId);
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static boolean staffRentContractAssignmentExists(CreatedContract contract) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND staff_id = ? AND customer_id = ? AND building_id = ?",
            contract.id(),
            contract.staff().id(),
            contract.customer().id(),
            contract.building().id());
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static boolean staffSaleContractAssignmentExists(CreatedSaleContract saleContract) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM sale_contract WHERE id = ? AND staff_id = ? AND customer_id = ? AND building_id = ?",
            saleContract.id(),
            saleContract.staff().id(),
            saleContract.customer().id(),
            saleContract.building().id());
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static boolean customerRentContractAssignmentExists(CreatedContract contract) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND customer_id = ? AND building_id = ?",
            contract.id(),
            contract.customer().id(),
            contract.building().id());
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static boolean customerActiveContractExists(CreatedContract contract) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND customer_id = ? AND building_id = ? AND status = 'ACTIVE'",
            contract.id(),
            contract.customer().id(),
            contract.building().id());
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static void updateStaffBuildingAssignments(Long staffId, List<Long> buildingIds) {
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .contentType("application/json")
        .body(buildingIds)
        .put("/api/v1/admin/staff/" + staffId + "/assignments/buildings");
  }

  public static void updateStaffCustomerAssignments(Long staffId, List<Integer> customerIds) {
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .contentType("application/json")
        .body(customerIds)
        .put("/api/v1/admin/staff/" + staffId + "/assignments/customers");
  }

  public static boolean customerExists(Integer customerId) {
    if (customerId == null) {
      return false;
    }
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT id FROM customer WHERE id = ? LIMIT 1", customerId);
    return !rows.isEmpty();
  }

  public static boolean customerAccountExists(String username, String email) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT COUNT(*) AS count FROM customer WHERE username = ? OR email = ?", username, email);
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static void deleteCustomerByIdentity(String username, String email) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id FROM customer WHERE username = ? OR email = ? ORDER BY id DESC", username, email);
    for (Map<String, Object> row : rows) {
      deleteCustomer(((Number) row.get("id")).intValue());
    }
    MySqlDbClient.execute("DELETE FROM email_verification WHERE email = ?", email == null ? "" : email.toLowerCase(java.util.Locale.ROOT));
  }

  public static String latestVerificationStatus(String email, String purpose) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT status FROM email_verification WHERE email = ? AND purpose = ? ORDER BY created_at DESC LIMIT 1",
            email == null ? "" : email.toLowerCase(java.util.Locale.ROOT),
            purpose);
    return rows.isEmpty() ? "" : String.valueOf(rows.get(0).get("status"));
  }

  public static String latestVerificationSetupToken(String email, String purpose) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT setup_token FROM email_verification WHERE email = ? AND purpose = ? ORDER BY created_at DESC LIMIT 1",
            email == null ? "" : email.toLowerCase(java.util.Locale.ROOT),
            purpose);
    return rows.isEmpty() || rows.get(0).get("setup_token") == null ? "" : String.valueOf(rows.get(0).get("setup_token"));
  }

  public static CreatedCustomerRecord findCreatedCustomer(String username) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id FROM customer WHERE username = ? ORDER BY id DESC LIMIT 1", username);
    if (rows.isEmpty()) {
      return null;
    }
    return new CreatedCustomerRecord(((Number) rows.get(0).get("id")).intValue());
  }

  public static CreatedStaffRecord findCreatedStaff(String username) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id, role FROM staff WHERE username = ? ORDER BY id DESC LIMIT 1", username);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new CreatedStaffRecord(((Number) row.get("id")).longValue(), String.valueOf(row.get("role")));
  }

  public static boolean staffExists(Long staffId) {
    if (staffId == null) {
      return false;
    }
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT id FROM staff WHERE id = ? LIMIT 1", staffId);
    return !rows.isEmpty();
  }

  public static String readProfileUsername(String table, Number userId) {
    return singleString("SELECT username FROM " + profileTable(table) + " WHERE id = ?", userId);
  }

  public static String readProfilePhone(String table, Number userId) {
    return singleString("SELECT phone FROM " + profileTable(table) + " WHERE id = ?", userId);
  }

  public static String readProfilePasswordHash(String table, Number userId) {
    return singleString("SELECT password FROM " + profileTable(table) + " WHERE id = ?", userId);
  }

  public static boolean waitUntilCustomerDeleted(Integer customerId) {
    long deadline = System.currentTimeMillis() + AppConfig.get().expectTimeout().toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (!customerExists(customerId)) {
        return true;
      }
      sleepShort();
    }
    return !customerExists(customerId);
  }

  public static boolean waitUntilStaffDeleted(Long staffId) {
    long deadline = System.currentTimeMillis() + AppConfig.get().expectTimeout().toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (!staffExists(staffId)) {
        return true;
      }
      sleepShort();
    }
    return !staffExists(staffId);
  }

  public record CreatedStaff(Long id, String username, String password, String fullName, String email, String role) {}

  public record CreatedCustomerRecord(Integer id) {}

  public record CreatedStaffRecord(Long id, String role) {}

  public static CreatedBuilding createBuilding(String transactionType) {
    Map<String, Object> payload = TestDataFactory.buildBuildingPayload(transactionType);
    String name = String.valueOf(payload.get("name"));
    String normalizedTransactionType = String.valueOf(payload.get("transactionType"));

    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(payload)
            .post("/api/v1/admin/buildings");

    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create building fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }

    Long id = findBuildingIdByName(name);
    if (id == null) {
      throw new IllegalStateException("Cannot resolve created building id by name: " + name);
    }
    return new CreatedBuilding(id, name, normalizedTransactionType);
  }

  public static AssignableScenario createAssignableScenario(String transactionType) {
    CreatedStaff staff = createStaff("STAFF");
    CreatedBuilding building = createBuilding(transactionType);
    updateStaffBuildingAssignments(staff.id(), List.of(building.id()));
    CreatedCustomer customer = createCustomer(staff.id().intValue());
    updateStaffCustomerAssignments(staff.id(), List.of(customer.id()));
    return new AssignableScenario(staff, customer, building);
  }

  public static CreatedContract createContract() {
    AssignableScenario scenario = createAssignableScenario("FOR_RENT");
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "customerId", scenario.customer().id(),
                    "buildingId", scenario.building().id(),
                    "staffId", scenario.staff().id(),
                    "rentPrice", 1_000_000,
                    "rentArea", 50,
                    "startDate", "2026-01-01",
                    "endDate", "2027-12-31",
                    "status", "ACTIVE"))
            .post("/api/v1/admin/contracts");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create contract fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }
    ContractRecord record = findCreatedContract(scenario.customer().id(), scenario.building().id());
    if (record == null) {
      throw new IllegalStateException("Cannot resolve created contract id.");
    }
    return new CreatedContract(record.id(), scenario.staff(), scenario.customer(), scenario.building());
  }

  public static PropertyRequestScenario createPropertyRequestScenario(String requestType) {
    String buildingType = "BUY".equals(requestType) ? "FOR_SALE" : "FOR_RENT";
    CreatedStaff staff = createStaff("STAFF");
    CreatedBuilding building = createBuilding(buildingType);
    updateStaffBuildingAssignments(staff.id(), List.of(building.id()));
    CreatedCustomer customer = createCustomer(staff.id().intValue());
    updateStaffCustomerAssignments(staff.id(), List.of(customer.id()));

    Map<String, Object> payload = new java.util.LinkedHashMap<>();
    payload.put("buildingId", building.id());
    payload.put("requestType", requestType);
    payload.put("offeredPrice", "BUY".equals(requestType) ? 3_000_000_000L : 1_000_000L);
    payload.put("message", "Selenium property request");
    if ("RENT".equals(requestType)) {
      payload.put("desiredArea", 50);
      payload.put("desiredStartDate", "2026-06-01");
      payload.put("desiredEndDate", "2027-06-01");
    }

    Response response =
        TestStateSession.authenticatedAs(customer.username(), customer.password())
            .contentType("application/json")
            .body(payload)
            .post("/api/v1/customer/property-requests");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create property request fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }

    Long requestId = findCreatedPropertyRequest(customer.id(), building.id());
    if (requestId == null) {
      throw new IllegalStateException("Cannot resolve created property request id.");
    }
    return new PropertyRequestScenario(requestId, requestType, staff, customer, building);
  }

  public static Long createLinkedRentContract(PropertyRequestScenario scenario) {
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "customerId", scenario.customer().id(),
                    "buildingId", scenario.building().id(),
                    "staffId", scenario.staff().id(),
                    "rentPrice", 1_000_000,
                    "rentArea", 50,
                    "startDate", "2026-06-01",
                    "endDate", "2027-06-01",
                    "status", "ACTIVE"))
            .post("/api/v1/admin/contracts");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create linked rent contract. HTTP " + response.statusCode() + ": " + response.asString());
    }
    ContractRecord record = findCreatedContract(scenario.customer().id(), scenario.building().id());
    return record == null ? null : record.id();
  }

  public static Long createLinkedSaleContract(PropertyRequestScenario scenario) {
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "buildingId", scenario.building().id(),
                    "customerId", scenario.customer().id(),
                    "staffId", scenario.staff().id(),
                    "salePrice", 3_000_000_000L,
                    "note", "Selenium property request sale contract"))
            .post("/api/v1/admin/sale-contracts");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create linked sale contract. HTTP " + response.statusCode() + ": " + response.asString());
    }
    SaleContractRecord record = findCreatedSaleContract(scenario.customer().id(), scenario.building().id());
    return record == null ? null : record.id();
  }

  public static Long approveRentPropertyRequest(PropertyRequestScenario scenario) {
    Long contractId = createLinkedRentContract(scenario);
    approvePropertyRequest(scenario.id(), Map.of("contractId", contractId));
    return contractId;
  }

  public static Long approveBuyPropertyRequest(PropertyRequestScenario scenario) {
    Long saleContractId = createLinkedSaleContract(scenario);
    approvePropertyRequest(scenario.id(), Map.of("saleContractId", saleContractId));
    return saleContractId;
  }

  public static void rejectPropertyRequest(Long id, String reason) {
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(Map.of("reason", reason))
            .post("/api/v1/admin/property-requests/" + id + "/reject");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot reject property request. HTTP " + response.statusCode() + ": " + response.asString());
    }
  }

  private static void approvePropertyRequest(Long id, Map<String, Long> payload) {
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(payload)
            .post("/api/v1/admin/property-requests/" + id + "/approve");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot approve property request. HTTP " + response.statusCode() + ": " + response.asString());
    }
  }

  public static void deleteContract(Long id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/contracts/" + id);
    MySqlDbClient.execute("DELETE FROM utility_meter WHERE contract_id = ?", id);
    MySqlDbClient.execute("DELETE FROM invoice_detail WHERE invoice_id IN (SELECT id FROM invoice WHERE contract_id = ?)", id);
    MySqlDbClient.execute("DELETE FROM invoice WHERE contract_id = ?", id);
    MySqlDbClient.execute("DELETE FROM contract WHERE id = ?", id);
  }

  public static InvoiceRecord createInvoice(CreatedContract contract) {
    InvoicePeriod period = previousInvoicePeriod();
    Map<String, Object> payload = buildInvoicePayload(contract, period, 18, 7, "PENDING");
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(payload)
            .post("/api/v1/admin/invoices");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create invoice fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }
    InvoiceRecord invoice = findCreatedInvoiceForPeriod(contract, period);
    if (invoice == null) {
      throw new IllegalStateException("Cannot resolve created invoice id.");
    }
    return invoice;
  }

  public static InvoiceRecord createInvoice(CreatedContract contract, InvoicePeriod period, int electricityUsage, int waterUsage) {
    Map<String, Object> payload = buildInvoicePayload(contract, period, electricityUsage, waterUsage, "PENDING");
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(payload)
            .post("/api/v1/admin/invoices");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create invoice fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }
    return findCreatedInvoiceForPeriod(contract, period);
  }

  public static Map<String, Object> buildInvoicePayload(CreatedContract contract, InvoicePeriod period, int electricityUsage, int waterUsage, String status) {
    long rentAmount = 50L * 1_000_000L;
    long electricityAmount = electricityUsage * 3_500L;
    long waterAmount = waterUsage * 15_000L;
    java.util.List<Map<String, Object>> details =
        java.util.List.of(
            Map.of("description", "Tien thue mat bang", "amount", rentAmount),
            Map.of("description", "Phi dich vu", "amount", 100_000),
            Map.of("description", "Phi gui o to", "amount", 50_000),
            Map.of("description", "Phi gui xe may", "amount", 20_000),
            Map.of("description", "Phi dien", "amount", electricityAmount),
            Map.of("description", "Phi nuoc", "amount", waterAmount));
    long total = rentAmount + 100_000 + 50_000 + 20_000 + electricityAmount + waterAmount;
    return new java.util.LinkedHashMap<>(
        Map.of(
            "contractId", contract.id(),
            "customerId", contract.customer().id(),
            "month", period.month(),
            "year", period.year(),
            "status", status,
            "dueDate", period.dueDate(),
            "totalAmount", total,
            "details", details,
            "electricityUsage", electricityUsage,
            "waterUsage", waterUsage));
  }

  public static InvoicePeriod previousInvoicePeriod() {
    java.time.LocalDate invoiceDate = java.time.LocalDate.now().minusMonths(1);
    java.time.LocalDate dueDate = invoiceDate.plusMonths(1).withDayOfMonth(15);
    return new InvoicePeriod(invoiceDate.getMonthValue(), invoiceDate.getYear(), dueDate.toString());
  }

  public static InvoiceRecord findCreatedInvoiceForPeriod(CreatedContract contract, InvoicePeriod period) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT id, status FROM invoice WHERE contract_id = ? AND customer_id = ? AND month = ? AND year = ? ORDER BY id DESC LIMIT 1",
            contract.id(),
            contract.customer().id(),
            period.month(),
            period.year());
    if (rows.isEmpty()) return null;
    Map<String, Object> row = rows.get(0);
    return new InvoiceRecord(((Number) row.get("id")).longValue(), contract.id(), contract.customer().id(), period.month(), period.year(), String.valueOf(row.get("status")));
  }

  public static void deleteInvoice(Long id) {
    if (id == null) return;
    MySqlDbClient.execute("DELETE FROM invoice_detail WHERE invoice_id = ?", id);
    MySqlDbClient.execute("DELETE FROM invoice WHERE id = ?", id);
  }

  public static Long findCreatedPropertyRequest(Integer customerId, Long buildingId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT id FROM property_request WHERE customer_id = ? AND building_id = ? ORDER BY id DESC LIMIT 1",
            customerId,
            buildingId);
    return rows.isEmpty() ? null : ((Number) rows.get(0).get("id")).longValue();
  }

  public static void deletePropertyRequest(Long id) {
    if (id == null) return;
    MySqlDbClient.execute("DELETE FROM property_request WHERE id = ?", id);
  }

  public static PropertyRequestState readPropertyRequestState(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT status, admin_note, contract_id, sale_contract_id FROM property_request WHERE id = ?",
            id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new PropertyRequestState(
        String.valueOf(row.get("status")),
        row.get("admin_note") == null ? "" : String.valueOf(row.get("admin_note")),
        row.get("contract_id") == null ? null : ((Number) row.get("contract_id")).longValue(),
        row.get("sale_contract_id") == null ? null : ((Number) row.get("sale_contract_id")).longValue());
  }

  public static boolean invoiceExists(Long id) {
    return id != null && !MySqlDbClient.query("SELECT id FROM invoice WHERE id = ?", id).isEmpty();
  }

  public static String readInvoiceStatus(Long id) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT status FROM invoice WHERE id = ?", id);
    return rows.isEmpty() ? "" : String.valueOf(rows.get(0).get("status"));
  }

  public static String readInvoiceDueDate(Long id) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT DATE_FORMAT(due_date, '%Y-%m-%d') AS due_date FROM invoice WHERE id = ?", id);
    return rows.isEmpty() ? "" : String.valueOf(rows.get(0).get("due_date"));
  }

  public static String readInvoiceEditState(Long id) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT status, DATE_FORMAT(due_date, '%Y-%m-%d') AS due_date FROM invoice WHERE id = ?", id);
    if (rows.isEmpty()) {
      return "";
    }
    return rows.get(0).get("status") + "|" + rows.get(0).get("due_date");
  }

  public static long invoicePeriodCount(CreatedContract contract, InvoiceRecord invoice) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM invoice WHERE contract_id = ? AND customer_id = ? AND month = ? AND year = ?",
            contract.id(),
            contract.customer().id(),
            invoice.month(),
            invoice.year());
    return rows.isEmpty() ? 0 : ((Number) rows.get(0).get("count")).longValue();
  }

  public static void markInvoicePaid(Long id) {
    MySqlDbClient.execute("UPDATE invoice SET status = 'PAID' WHERE id = ?", id);
  }

  public static void markInvoicePaidByBankQr(Long id) {
    MySqlDbClient.execute(
        "UPDATE invoice SET status = 'PAID', payment_method = 'BANK_QR', transaction_code = ?, paid_date = NOW() WHERE id = ?",
        "E2E-TX-" + id,
        id);
  }

  public static long readInvoiceTotalAmount(Long id) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT total_amount FROM invoice WHERE id = ?", id);
    return rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_amount")).longValue();
  }

  public static InvoicePaymentState readInvoicePaymentState(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT status, payment_method, transaction_code, paid_date FROM invoice WHERE id = ?",
            id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new InvoicePaymentState(
        String.valueOf(row.get("status")),
        String.valueOf(row.get("payment_method")),
        String.valueOf(row.get("transaction_code")),
        row.get("paid_date") == null ? "" : String.valueOf(row.get("paid_date")));
  }

  public static long paidInvoicePeriodCount(Long invoiceId, int month, int year) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM invoice WHERE id = ? AND month = ? AND year = ? AND status = 'PAID'",
            invoiceId,
            month,
            year);
    return rows.isEmpty() ? 0 : ((Number) rows.get(0).get("count")).longValue();
  }

  public static CreatedSaleContract createSaleContract() {
    AssignableScenario scenario = createAssignableScenario("FOR_SALE");
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "buildingId", scenario.building().id(),
                    "customerId", scenario.customer().id(),
                    "staffId", scenario.staff().id(),
                    "salePrice", 3_000_000_000L,
                    "note", "Selenium sale contract test"))
            .post("/api/v1/admin/sale-contracts");
    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create sale contract fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }
    SaleContractRecord record = findCreatedSaleContract(scenario.customer().id(), scenario.building().id());
    if (record == null) {
      throw new IllegalStateException("Cannot resolve created sale contract id.");
    }
    return new CreatedSaleContract(record.id(), scenario.staff(), scenario.customer(), scenario.building());
  }

  public static void deleteSaleContract(Long id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/sale-contracts/" + id);
    MySqlDbClient.execute("DELETE FROM sale_contract WHERE id = ?", id);
  }

  public static SaleContractRecord findCreatedSaleContract(Integer customerId, Long buildingId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT id, sale_price FROM sale_contract WHERE customer_id = ? AND building_id = ? ORDER BY id DESC LIMIT 1",
            customerId,
            buildingId);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new SaleContractRecord(((Number) row.get("id")).longValue(), ((Number) row.get("sale_price")).longValue());
  }

  public static boolean saleContractExists(Long id) {
    return id != null && !MySqlDbClient.query("SELECT id FROM sale_contract WHERE id = ?", id).isEmpty();
  }

  public static String readSaleContractTransferDate(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT DATE_FORMAT(transfer_date, '%Y-%m-%d') AS transfer_date FROM sale_contract WHERE id = ?", id);
    return rows.isEmpty() || rows.get(0).get("transfer_date") == null ? null : String.valueOf(rows.get(0).get("transfer_date"));
  }

  public static ContractRecord findCreatedContract(Integer customerId, Long buildingId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            """
            SELECT id, rent_price, DATE_FORMAT(start_date, '%Y-%m-%d') AS start_date, DATE_FORMAT(end_date, '%Y-%m-%d') AS end_date
            FROM contract
            WHERE customer_id = ? AND building_id = ?
            ORDER BY id DESC
            LIMIT 1
            """,
            customerId,
            buildingId);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new ContractRecord(
        ((Number) row.get("id")).longValue(),
        ((Number) row.get("rent_price")).longValue(),
        String.valueOf(row.get("start_date")),
        String.valueOf(row.get("end_date")));
  }

  public static boolean contractExists(Long id) {
    return id != null && !MySqlDbClient.query("SELECT id FROM contract WHERE id = ?", id).isEmpty();
  }

  public static boolean contractWithPriceExists(Integer customerId, Long buildingId, long rentPrice) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM contract WHERE customer_id = ? AND building_id = ? AND rent_price = ?",
            customerId,
            buildingId,
            rentPrice);
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() > 0;
  }

  public static ContractEditState readContractEditState(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT rent_price, DATE_FORMAT(end_date, '%Y-%m-%d') AS end_date, status FROM contract WHERE id = ?", id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new ContractEditState(
        ((Number) row.get("rent_price")).longValue(), String.valueOf(row.get("end_date")), String.valueOf(row.get("status")));
  }

  public static void expireContract(Long id) {
    MySqlDbClient.execute("UPDATE contract SET status = 'EXPIRED' WHERE id = ?", id);
  }

  public static void deleteBuilding(Long id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/buildings/" + id);
    MySqlDbClient.execute("DELETE FROM contract WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM sale_contract WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM rent_area WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM assignment_building WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM nearby_amenity WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM planning_map WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM legal_authority WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM supplier WHERE building_id = ?", id);
    MySqlDbClient.execute("DELETE FROM building WHERE id = ?", id);
  }

  public static void deleteAdditionalInfoRecord(String type, Long id) {
    if (type == null || id == null) {
      return;
    }
    String table =
        switch (type) {
          case "legal" -> "legal_authority";
          case "amenity" -> "nearby_amenity";
          case "planning" -> "planning_map";
          case "supplier" -> "supplier";
          default -> throw new IllegalArgumentException("Unsupported additional info type: " + type);
        };
    MySqlDbClient.execute("DELETE FROM " + table + " WHERE id = ?", id);
  }

  public static Long findLegalAuthorityId(Long buildingId, String authorityName) {
    return singleLong(
        "SELECT id FROM legal_authority WHERE building_id = ? AND authority_name = ? ORDER BY id DESC LIMIT 1",
        buildingId,
        authorityName);
  }

  public static LegalAuthorityRecord readLegalAuthority(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT authority_name, authority_type FROM legal_authority WHERE id = ?", id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new LegalAuthorityRecord(String.valueOf(row.get("authority_name")), String.valueOf(row.get("authority_type")));
  }

  public static Long findAmenityId(Long buildingId, String name) {
    return singleLong(
        "SELECT id FROM nearby_amenity WHERE building_id = ? AND name = ? ORDER BY id DESC LIMIT 1",
        buildingId,
        name);
  }

  public static AmenityRecord readAmenity(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT name, amenity_type, distance_meter FROM nearby_amenity WHERE id = ?", id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new AmenityRecord(
        String.valueOf(row.get("name")),
        String.valueOf(row.get("amenity_type")),
        ((Number) row.get("distance_meter")).intValue());
  }

  public static Long findSupplierId(Long buildingId, String name) {
    return singleLong("SELECT id FROM supplier WHERE building_id = ? AND name = ? ORDER BY id DESC LIMIT 1", buildingId, name);
  }

  public static SupplierRecord readSupplier(Long id) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT name, service_type, email FROM supplier WHERE id = ?", id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new SupplierRecord(String.valueOf(row.get("name")), String.valueOf(row.get("service_type")), String.valueOf(row.get("email")));
  }

  public static Long findPlanningMapId(Long buildingId, String mapType) {
    return singleLong(
        "SELECT id FROM planning_map WHERE building_id = ? AND map_type = ? ORDER BY id DESC LIMIT 1",
        buildingId,
        mapType);
  }

  public static PlanningMapRecord readPlanningMap(Long id) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT map_type, issued_by, image_url FROM planning_map WHERE id = ?", id);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new PlanningMapRecord(String.valueOf(row.get("map_type")), String.valueOf(row.get("issued_by")), String.valueOf(row.get("image_url")));
  }

  public static boolean planningMapExists(Long id) {
    return id != null && !MySqlDbClient.query("SELECT id FROM planning_map WHERE id = ?", id).isEmpty();
  }

  public static int planningMapCount(Long buildingId) {
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT COUNT(*) AS total FROM planning_map WHERE building_id = ?", buildingId);
    return rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total")).intValue();
  }

  public static boolean buildingNameMatches(Long buildingId, String buildingName) {
    if (buildingId == null || buildingName == null) {
      return false;
    }
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT COUNT(*) AS count FROM building WHERE id = ? AND name = ?", buildingId, buildingName);
    return !rows.isEmpty() && ((Number) rows.get(0).get("count")).longValue() == 1;
  }

  public static CreatedBuildingFromForm findBuildingCreatedFromForm(String buildingName) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            """
            SELECT id, transaction_type, floor_area, tax_code
            FROM building
            WHERE name = ?
            ORDER BY id DESC
            LIMIT 1
            """,
            buildingName);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new CreatedBuildingFromForm(
        ((Number) row.get("id")).longValue(),
        String.valueOf(row.get("transaction_type")),
        ((Number) row.get("floor_area")).longValue(),
        String.valueOf(row.get("tax_code")));
  }

  public static BuildingEditState findBuildingEditState(Long buildingId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT name, floor_area, rent_price FROM building WHERE id = ? LIMIT 1", buildingId);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    return new BuildingEditState(
        String.valueOf(row.get("name")),
        ((Number) row.get("floor_area")).longValue(),
        ((Number) row.get("rent_price")).longValue());
  }

  public static boolean buildingExists(Long buildingId) {
    if (buildingId == null) {
      return false;
    }
    List<Map<String, Object>> rows = MySqlDbClient.query("SELECT id FROM building WHERE id = ? LIMIT 1", buildingId);
    return !rows.isEmpty();
  }

  public static boolean waitUntilBuildingDeleted(Long buildingId) {
    long deadline = System.currentTimeMillis() + AppConfig.get().expectTimeout().toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (!buildingExists(buildingId)) {
        return true;
      }
      try {
        Thread.sleep(250);
      } catch (InterruptedException error) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
    return !buildingExists(buildingId);
  }

  private static Long findBuildingIdByName(String name) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id FROM building WHERE name = ? ORDER BY id DESC LIMIT 1", name);
    if (rows.isEmpty()) {
      return null;
    }
    return ((Number) rows.get(0).get("id")).longValue();
  }

  private static Long findStaffIdByUsername(String username) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id FROM staff WHERE username = ? ORDER BY id DESC LIMIT 1", username);
    if (rows.isEmpty()) {
      return null;
    }
    return ((Number) rows.get(0).get("id")).longValue();
  }

  private static Integer findCustomerIdByUsername(String username) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query("SELECT id FROM customer WHERE username = ? ORDER BY id DESC LIMIT 1", username);
    if (rows.isEmpty()) {
      return null;
    }
    return ((Number) rows.get(0).get("id")).intValue();
  }

  private static void sleepShort() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for test state.", error);
    }
  }

  public record CreatedBuilding(Long id, String name, String transactionType) {}

  public record AssignableScenario(CreatedStaff staff, CreatedCustomer customer, CreatedBuilding building) {}

  public record CreatedContract(Long id, CreatedStaff staff, CreatedCustomer customer, CreatedBuilding building) {}

  public record CreatedSaleContract(Long id, CreatedStaff staff, CreatedCustomer customer, CreatedBuilding building) {}

  public record PropertyRequestScenario(Long id, String requestType, CreatedStaff staff, CreatedCustomer customer, CreatedBuilding building) {}

  public record ContractRecord(Long id, Long rentPrice, String startDate, String endDate) {}

  public record SaleContractRecord(Long id, Long salePrice) {}

  public record InvoicePeriod(Integer month, Integer year, String dueDate) {}

  public record InvoiceRecord(Long id, Long contractId, Integer customerId, Integer month, Integer year, String status) {}

  public record InvoicePaymentState(String status, String paymentMethod, String transactionCode, String paidDate) {}

  public record PropertyRequestState(String status, String adminNote, Long contractId, Long saleContractId) {}

  public record ContractEditState(Long rentPrice, String endDate, String status) {}

  public record CreatedBuildingFromForm(Long id, String transactionType, Long floorArea, String taxCode) {}

  public record BuildingEditState(String name, Long floorArea, Long rentPrice) {}

  public record LegalAuthorityRecord(String authorityName, String authorityType) {}

  public record AmenityRecord(String name, String amenityType, Integer distanceMeter) {}

  public record SupplierRecord(String name, String serviceType, String email) {}

  public record PlanningMapRecord(String mapType, String issuedBy, String imageUrl) {}

  private static Long singleLong(String sql, Object... params) {
    List<Map<String, Object>> rows = MySqlDbClient.query(sql, params);
    if (rows.isEmpty()) {
      return null;
    }
    return ((Number) rows.get(0).get("id")).longValue();
  }

  private static String singleString(String sql, Object... params) {
    List<Map<String, Object>> rows = MySqlDbClient.query(sql, params);
    if (rows.isEmpty()) {
      return "";
    }
    Object value = rows.get(0).values().iterator().next();
    return value == null ? "" : String.valueOf(value);
  }

  private static String profileTable(String table) {
    return switch (table) {
      case "staff" -> "staff";
      case "customer" -> "customer";
      default -> throw new IllegalArgumentException("Unsupported profile table: " + table);
    };
  }
}
