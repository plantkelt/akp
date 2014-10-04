package net.plantkelt.akp.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.type.Type;

public class MaxIdGenerator implements IdentifierGenerator, Configurable {

	private Type identifierType;
	private String tableName;
	private String columnName;

	@Override
	public void configure(Type type, Properties params, Dialect dialect) {
		identifierType = type;
		tableName = (String) params.getProperty("target_table");
		columnName = (String) params.getProperty("target_column");
	}

	@Override
	public synchronized Serializable generate(SessionImplementor session,
			Object object) {
		return generateHolder(session).makeValue();
	}

	protected IntegralDataTypeHolder generateHolder(SessionImplementor session) {
		Connection connection = session.connection();
		try {
			IntegralDataTypeHolder value = IdentifierGeneratorHelper
					.getIntegralDataTypeHolder(identifierType
							.getReturnedClass());
			String sql = "select max(" + columnName + ") from " + tableName;
			PreparedStatement qps = connection.prepareStatement(sql);
			try {
				ResultSet rs = qps.executeQuery();
				if (rs.next())
					value.initialize(rs, 1);
				else
					value.initialize(1);
				rs.close();
			} finally {
				qps.close();
			}
			return value.copy().increment();
		} catch (SQLException e) {
			throw new IdentifierGenerationException(
					"Can't select max id value", e);
		}
	}
}
