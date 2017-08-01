package cn.wuxia.common.spring.orm;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

public class DataJdbcDao {

	@Resource
	private JdbcTemplate jdbcTemplate;

	/**
	 * update by sql, jdbcTemplate mode
	 * 
	 * @param sql
	 * @param objs
	 */
	public void saveOrUpdate(String sql, Object... objs) {
		this.getJdbcTemplate().update(sql, objs);
	}

	/**
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}
