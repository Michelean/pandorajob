package tech.powerjob.server.persistence.external;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author: zmx
 * @date 2022/4/14
 */
public interface MonitorPartsRepository extends JpaRepository<MonitorPartsDO, Long> {


    @Query(nativeQuery = true, value = "SELECT * FROM basic_monitor_parts WHERE wf_id = ( SELECT wf_id FROM basic_wf WHERE wf_scadaid = ?1 ) AND bsd_id IN ( SELECT bsd_id FROM basic_system_dictionary WHERE bsd_type_code = 'MONITOR_TYPE' AND bsd_option_value IN ( 'thing_device_wind_turbine' ) )")
    List<MonitorPartsDO> findByWfScadaid(String wfScadaid);



    @Query(nativeQuery = true, value = "SELECT * FROM basic_monitor_parts WHERE wf_id = ?1 AND bsd_id IN ( SELECT bsd_id FROM basic_system_dictionary WHERE bsd_type_code = 'MONITOR_TYPE' AND bsd_option_value IN ( 'thing_device_wind_turbine' ) )")
    List<MonitorPartsDO> findByWfId(String wfId);
}
