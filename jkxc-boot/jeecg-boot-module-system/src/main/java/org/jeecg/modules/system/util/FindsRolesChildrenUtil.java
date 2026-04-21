package org.jeecg.modules.system.util;

import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.model.SysRoleTreeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * <P>
 * 对应角色的表,处理并查找树级数据
 * <P>
 * 
 * @Author Auto
 * @Date: 2025-12-23
 */
public class FindsRolesChildrenUtil {

    /**
     * wrapTreeDataToTreeList的子方法 ====1=====
     * 该方法是s将SysRole类型的list集合转换成SysRoleTreeModel类型的集合
     */
    public static List<SysRoleTreeModel> wrapTreeDataToTreeList(List<SysRole> recordList) {
        List<SysRoleTreeModel> records = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            SysRole role = recordList.get(i);
            records.add(new SysRoleTreeModel(role));
        }
        List<SysRoleTreeModel> tree = findChildren(records);
        setEmptyChildrenAsNull(tree);
        return tree;
    }

    /**
     * wrapTreeDataToTreeList的子方法 ====2=====
     * 该方法是找到并封装顶级父类的节点到TreeList集合
     */
    private static List<SysRoleTreeModel> findChildren(List<SysRoleTreeModel> recordList) {
        List<SysRoleTreeModel> treeList = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            SysRoleTreeModel branch = recordList.get(i);
            if (oConvertUtils.isEmpty(branch.getParentId())) {
                treeList.add(branch);
            }
        }
        getGrandChildren(treeList, recordList);
        return treeList;
    }

    /**
     * wrapTreeDataToTreeList的子方法====3====
     * 该方法是找到顶级父类下的所有子节点集合并封装到TreeList集合
     */
    private static void getGrandChildren(List<SysRoleTreeModel> treeList, List<SysRoleTreeModel> recordList) {
        for (int i = 0; i < treeList.size(); i++) {
            SysRoleTreeModel model = treeList.get(i);
            for (int i1 = 0; i1 < recordList.size(); i1++) {
                SysRoleTreeModel m = recordList.get(i1);
                if (m.getParentId() != null && m.getParentId().equals(model.getId())) {
                    model.getChildren().add(m);
                }
            }
            getGrandChildren(treeList.get(i).getChildren(), recordList);
        }
    }

    /**
     * wrapTreeDataToTreeList的子方法 ====4====
     * 该方法是将子节点为空的List集合设置为Null值
     */
    private static void setEmptyChildrenAsNull(List<SysRoleTreeModel> treeList) {
        for (int i = 0; i < treeList.size(); i++) {
            SysRoleTreeModel model = treeList.get(i);
            if (model.getChildren().size() == 0) {
                model.setChildren(null);
                model.setIsLeaf(true);
            } else {
                setEmptyChildrenAsNull(model.getChildren());
                model.setIsLeaf(false);
            }
        }
    }
}

