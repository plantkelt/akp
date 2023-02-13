package net.plantkelt.akp.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

public class MaxIdGenerator implements IdentifierGenerator, Configurable {

	private Type identifierType;
	private String tableName;
	private String columnName;

	@Override
	public void configure(Type type, Properties params,
			ServiceRegistry serviceRegistry) throws MappingException {
		identifierType = type;
		tableName = (String) params.getProperty("target_table");
		columnName = (String) params.getProperty("target_column");
	}

	@Override
	public Number generate(SharedSessionContractImplementor session,
			Object object) throws HibernateException {
		return generateHolder(session).makeValue();
	}

	protected IntegralDataTypeHolder generateHolder(
			SharedSessionContractImplementor session) {
		Connection connection = session.connection();
		try {
			IntegralDataTypeHolder value = IdentifierGeneratorHelper
					.getIntegralDataTypeHolder(
							identifierType.getReturnedClass());
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
			throw new IdentifierGenerationException("Can't select max id value",
					e);
		}
	}
}
