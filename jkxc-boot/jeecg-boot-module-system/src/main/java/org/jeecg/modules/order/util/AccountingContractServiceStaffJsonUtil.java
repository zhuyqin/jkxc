package org.jeecg.modules.order.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.order.entity.GhAccountingContract;
import org.jeecg.modules.system.entity.SysAuditTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 从订单审批任务推导代账合同 {@link GhAccountingContract#getServiceStaffJson()}（财税主管、财税顾问、主办会计）。
 */
public final class AccountingContractServiceStaffJsonUtil {

	private static final Logger log = LoggerFactory.getLogger(AccountingContractServiceStaffJsonUtil.class);

	private AccountingContractServiceStaffJsonUtil() {
	}

	/**
	 * 将审批任务中解析出的服务人员写入合同；derived 为空时不修改合同。
	 *
	 * @param onlySupervisorAndConsultant true：仅写入「财税主管」「财税顾问」（审批转代账合同场景）；false：含「主办会计」等与审批角色匹配的全部键（历史回填等）
	 */
	public static void applyDerivedToContract(GhAccountingContract contract, List<SysAuditTask> tasks,
			boolean onlySupervisorAndConsultant) {
		if (contract == null) {
			return;
		}
		JSONObject derived = deriveServiceStaffJsonFromAuditTasks(tasks);
		if (onlySupervisorAndConsultant) {
			JSONObject filtered = new JSONObject();
			if (derived.containsKey("财税主管")) {
				filtered.put("财税主管", derived.get("财税主管"));
			}
			if (derived.containsKey("财税顾问")) {
				filtered.put("财税顾问", derived.get("财税顾问"));
			}
			derived = filtered;
		}
		if (derived.isEmpty()) {
			return;
		}
		JSONObject merged = mergeServiceStaffJsonPreserveKeys(contract.getServiceStaffJson(), derived);
		contract.setServiceStaffJson(merged.isEmpty() ? null : merged.toJSONString());
	}

	public static JSONObject deriveServiceStaffJsonFromAuditTasks(List<SysAuditTask> tasks) {
		JSONObject jo = new JSONObject();
		if (tasks == null || tasks.isEmpty()) {
			return jo;
		}
		List<SysAuditTask> sorted = new ArrayList<>(tasks);
		sorted.sort(Comparator
				.comparing(SysAuditTask::getStepOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(SysAuditTask::getId, Comparator.nullsLast(String::compareTo)));
		for (SysAuditTask t : sorted) {
			String key = mapAuditRoleNameToServiceStaffKey(t.getCurrentRoleName());
			if (key == null) {
				continue;
			}
			String person = extractAuditTaskPersonName(t);
			if (oConvertUtils.isEmpty(person)) {
				continue;
			}
			jo.put(key, person);
		}
		return jo;
	}

	/** 以 derived 中的键更新 merged，其余键保留原 JSON（如已维护的主办会计） */
	public static JSONObject mergeServiceStaffJsonPreserveKeys(String existingJson, JSONObject derived) {
		JSONObject merged = new JSONObject();
		if (oConvertUtils.isNotEmpty(existingJson)) {
			try {
				JSONObject parsed = JSON.parseObject(existingJson);
				if (parsed != null) {
					merged.putAll(parsed);
				}
			} catch (Exception e) {
				log.warn("解析 service_staff_json 失败，将按审批结果重写: {}", e.getMessage());
			}
		}
		for (String key : derived.keySet()) {
			merged.put(key, derived.get(key));
		}
		return merged;
	}

	static String mapAuditRoleNameToServiceStaffKey(String roleName) {
		if (oConvertUtils.isEmpty(roleName)) {
			return null;
		}
		String r = roleName.trim();
		if (r.contains("财税主管")) {
			return "财税主管";
		}
		if (r.contains("财税顾问")) {
			return "财税顾问";
		}
		if (r.contains("主办会计")) {
			return "主办会计";
		}
		return null;
	}

	static String extractAuditTaskPersonName(SysAuditTask task) {
		if (task == null) {
			return null;
		}
		String st = task.getTaskStatus();
		if ("pending".equals(st)) {
			return oConvertUtils.isNotEmpty(task.getAssignedUserName()) ? task.getAssignedUserName().trim() : null;
		}
		if ("approved".equals(st) || "rejected".equals(st)) {
			return oConvertUtils.isNotEmpty(task.getAuditUserName()) ? task.getAuditUserName().trim() : null;
		}
		return oConvertUtils.isNotEmpty(task.getAuditUserName()) ? task.getAuditUserName().trim() : null;
	}
}
