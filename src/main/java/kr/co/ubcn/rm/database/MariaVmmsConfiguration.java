package kr.co.ubcn.rm.database;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@MapperScan(value="kr.co.ubcn.rm.mapper.mariaVmms", sqlSessionFactoryRef="mariaVmmsSqlSessionFactory")
@EnableTransactionManagement
public class MariaVmmsConfiguration {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Bean(name="mariaVmmsDataSource")
	@ConfigurationProperties(prefix="spring.maria.vmms.datasource")
	public DataSource mariaVmmsDataSource() {
		//application.properties에서 정의한 DB 연결 정보를 빌드
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="mariaVmmsSqlSessionFactory")
	public SqlSessionFactory mariaVmmsSqlSessionFactory(@Qualifier("mariaVmmsDataSource") DataSource mariaVmmsDataSource) throws Exception{
		//세션 생성 시, 빌드된 DataSource를 세팅하고 SQL문을 관리할 mapper.xml의 경로를 알려준다.
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(mariaVmmsDataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("file:config/mapper/*.xml"));
		
		sqlSessionFactoryBean.getObject().getConfiguration().setJdbcTypeForNull(JdbcType.NULL);  //mybatis null 적용
		
		return sqlSessionFactoryBean.getObject();
	}
	
	@Bean(name = "vmmsTransactionManager")
	public PlatformTransactionManager vmmsTransactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(mariaVmmsDataSource());
		transactionManager.setGlobalRollbackOnParticipationFailure(false);
		return transactionManager;
	}

	@Bean(name="mariaVmmsSqlSessionTemplate")
	public SqlSessionTemplate mariaVmmsSqlSessionTemplate(@Qualifier("mariaVmmsSqlSessionFactory") SqlSessionFactory mariaVmmsSqlSessionFactory) throws Exception{
		return new SqlSessionTemplate(mariaVmmsSqlSessionFactory);
	}
	
}

