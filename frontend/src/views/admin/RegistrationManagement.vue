<template>
  <div>
    <h2>报名管理</h2>
    <el-table :data="registrations" style="width: 100%">
      <el-table-column prop="activityName" label="活动名称"></el-table-column>
      <el-table-column prop="userName" label="报名人"></el-table-column>
      <el-table-column prop="studentId" label="学号/工号"></el-table-column>
      <el-table-column prop="phone" label="联系电话"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 0 ? '待审核' : scope.row.status === 1 ? '已通过' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button size="mini" type="success" @click="handleAudit(scope.row.id, 1)" :disabled="scope.row.status !== 0">通过</el-button>
          <el-button size="mini" type="danger" @click="handleAudit(scope.row.id, 2)" :disabled="scope.row.status !== 0">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-size="pageSize"
      layout="total, prev, pager, next"
      :total="total">
    </el-pagination>
  </div>
</template>

<script>
import { getRegistrations, auditRegistration } from '@/api/activity';

export default {
  data() {
    return {
      registrations: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
    };
  },
  created() {
    this.fetchRegistrations();
  },
  methods: {
    fetchRegistrations() {
      getRegistrations(this.currentPage, this.pageSize).then(response => {
        this.registrations = response.data.records;
        this.total = response.data.total;
      });
    },
    handleAudit(id, status) {
      auditRegistration(id, status).then(() => {
        this.$message.success('操作成功');
        this.fetchRegistrations();
      });
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.fetchRegistrations();
    },
  },
};
</script>