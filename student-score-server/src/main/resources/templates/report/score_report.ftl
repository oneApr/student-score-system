<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: SimSun, SimHei, serif; font-size: 12px; color: #333; padding: 40px; }
        .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #2563eb; padding-bottom: 20px; }
        .header h1 { font-size: 20px; color: #1e293b; margin-bottom: 8px; }
        .header p { font-size: 12px; color: #64748b; }
        .meta-info { display: flex; justify-content: space-between; margin-bottom: 20px; font-size: 11px; color: #64748b; }
        .meta-item { display: inline-block; margin-right: 30px; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th { background-color: #f1f5f9; color: #334155; padding: 8px 6px; text-align: center; border: 1px solid #e2e8f0; font-size: 11px; }
        td { padding: 6px; text-align: center; border: 1px solid #e2e8f0; font-size: 11px; }
        tr:nth-child(even) { background-color: #f8fafc; }
        .footer { text-align: center; margin-top: 30px; font-size: 10px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 10px; }
        .pass-rate { color: #16a34a; font-weight: bold; }
        .avg-score { color: #2563eb; font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h1>${report.reportName!''}</h1>
        <p>${report.reportDesc!''}</p>
    </div>

    <div class="meta-info">
        <span class="meta-item">报表类型: ${report.reportType!''}</span>
        <span class="meta-item">关联学期: ${term!''}</span>
        <span class="meta-item">生成时间: ${generatedTime!''}</span>
    </div>

    <table>
        <thead>
            <tr>
                <th>序号</th>
                <th>课程代码</th>
                <th>课程名称</th>
                <th>班级</th>
                <th>学生数</th>
                <th>平均分</th>
                <th>最高分</th>
                <th>最低分</th>
                <th>及格率</th>
                <th>优秀率</th>
            </tr>
        </thead>
        <tbody>
            <#if courseStats?? && (courseStats?size > 0)>
                <#list courseStats as item>
                <tr>
                    <td>${item_index + 1}</td>
                    <td>${item.courseCode!''}</td>
                    <td>${item.courseName!''}</td>
                    <td>${item.className!''}</td>
                    <td>${item.studentCount!0}</td>
                    <td class="avg-score">${item.avgScore!0}</td>
                    <td>${item.maxScore!0}</td>
                    <td>${item.minScore!0}</td>
                    <td class="pass-rate">${item.passRate!'0%'}</td>
                    <td>${item.excellentRate!'0%'}</td>
                </tr>
                </#list>
            <#else>
                <tr><td colspan="10" style="color: #94a3b8; padding: 20px;">暂无数据</td></tr>
            </#if>
        </tbody>
    </table>

    <div class="footer">
        <p>本报表由学生成绩管理系统自动生成 &#160; | &#160; ${generatedTime!''}</p>
    </div>
</body>
</html>
