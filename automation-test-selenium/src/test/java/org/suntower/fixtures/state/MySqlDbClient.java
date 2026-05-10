package org.suntower.fixtures.state;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.suntower.core.AppConfig;

public final class MySqlDbClient {
  private MySqlDbClient() {}

  public static List<Map<String, Object>> query(String sql, Object... params) {
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      bind(statement, params);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int columns = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          for (int index = 1; index <= columns; index++) {
            row.put(resultSet.getMetaData().getColumnLabel(index), resultSet.getObject(index));
          }
          rows.add(row);
        }
        return rows;
      }
    } catch (SQLException error) {
      throw new IllegalStateException("Database query failed", error);
    }
  }

  public static int execute(String sql, Object... params) {
    try (Connection connection = connection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      bind(statement, params);
      return statement.executeUpdate();
    } catch (SQLException error) {
      throw new IllegalStateException("Database statement failed", error);
    }
  }

  private static Connection connection() throws SQLException {
    AppConfig config = AppConfig.get();
    return DriverManager.getConnection(config.dbJdbcUrl(), config.dbUsername(), config.dbPassword());
  }

  private static void bind(PreparedStatement statement, Object... params) throws SQLException {
    for (int index = 0; index < params.length; index++) {
      statement.setObject(index + 1, params[index]);
    }
  }
}
