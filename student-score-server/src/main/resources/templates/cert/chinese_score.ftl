<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="zh-CN" xml:lang="zh-CN">
<head>
  <meta charset="UTF-8"/>
  <style>
    body { font-family: "ChineseFont", "SimSun", "SimHei", serif; font-size: 12px; color: #1a1a1a; margin: 0; padding: 30px 40px; }
    h1.title { font-size: 20px; font-weight: bold; text-align: center; letter-spacing: 6px; margin: 0 0 4px 0; }
    p.subtitle { font-size: 12px; text-align: center; color: #555; margin: 0 0 12px 0; }
    hr.divider { border: none; border-top: 2px solid #1a3a6b; margin: 10px 0 14px 0; }
    table.info-table { width: 100%; margin-bottom: 14px; border: 1px solid #ddd; background: #fafafa; }
    table.info-table td { padding: 5px 8px; font-size: 12px; width: 33%; }
    table.info-table td span { color: #555; }
    table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
    th { background: #1a3a6b; color: white; padding: 7px 5px; font-size: 11px; font-weight: normal; text-align: center; border: 1px solid #1a3a6b; }
    td { padding: 5px 4px; font-size: 11px; text-align: center; border: 1px solid #ddd; }
    tr.alt td { background: #f7f9fc; }
    .score-good { color: #16a34a; font-weight: bold; }
    .score-ok   { color: #2563eb; }
    .score-bad  { color: #dc2626; }
    table.summary { width: 100%; margin-bottom: 14px; background: #f0f4ff; }
    table.summary td { padding: 8px 12px; font-size: 12px; border: none; }
    p.note { font-size: 11px; color: #555; margin: 0 0 8px 0; }
    p.footer { font-size: 11px; color: #888; text-align: center; border-top: 1px solid #ddd; padding-top: 8px; margin-top: 16px; }
    table.stamp-row td { border: none; text-align: right; padding: 0; }
    div.stamp { width: 88px; height: 88px; border: 2px solid #b91c1c; border-radius: 50%;
                color: #b91c1c; font-size: 11px; text-align: center; line-height: 1.4;
                padding-top: 28px; box-sizing: border-box;
                -fs-transform: rotate(-15deg); margin-left: auto; }
  </style>
</head>
<body>
  <h1 class="title">${universityName}成绩单</h1>
  <p class="subtitle">${universityName} - OFFICIAL TRANSCRIPT</p>
  <hr class="divider"/>

  <table class="info-table">
    <tr>
      <td><span>姓&#160;&#160;&#160;名：</span><strong>${studentName}</strong></td>
      <td><span>学&#160;&#160;&#160;号：</span>${studentNo}</td>
      <td><span>性&#160;&#160;&#160;别：</span>${gender}</td>
    </tr>
    <tr>
      <td><span>年&#160;&#160;&#160;级：</span>${grade!'-'}</td>
      <td><span>专&#160;&#160;&#160;业：</span>${major!'-'}</td>
      <td><span>班&#160;&#160;&#160;级：</span>${className!'-'}</td>
    </tr>
  </table>

  <table>
    <thead>
      <tr>
        <th>序号</th>
        <th>学期</th>
        <th>课程代码</th>
        <th>课程名称</th>
        <th>学分</th>
        <th>期末成绩</th>
        <th>总评成绩</th>
        <th>等级</th>
        <th>绩点</th>
      </tr>
    </thead>
    <tbody>
      <#list scores as s>
      <tr <#if (s?index % 2 == 1)>class="alt"</#if>>
        <td>${s?index + 1}</td>
        <td>${s.term!'-'}</td>
        <td>${s.courseCode!'-'}</td>
        <td style="text-align:left;padding-left:6px;">${s.courseName!'-'}</td>
        <td>${s.credit!'-'}</td>
        <td>
          <#if s.finalScore??>
            <#if (s.finalScore >= 90)><span class="score-good">${s.finalScore}</span>
            <#elseif (s.finalScore < 60)><span class="score-bad">${s.finalScore}</span>
            <#else><span class="score-ok">${s.finalScore}</span></#if>
          <#else>-</#if>
        </td>
        <td>
          <strong>
          <#if s.totalScore??>
            <#if (s.totalScore >= 90)><span class="score-good">${s.totalScore}</span>
            <#elseif (s.totalScore < 60)><span class="score-bad">${s.totalScore}</span>
            <#else><span class="score-ok">${s.totalScore}</span></#if>
          <#else>-</#if>
          </strong>
        </td>
        <td>${s.gradeLevel!'-'}</td>
        <td>${s.gpa!'-'}</td>
      </tr>
      </#list>
    </tbody>
  </table>

  <table class="summary">
    <tr>
      <td>已修课程总数：<strong>${totalCourses}</strong> 门</td>
      <td>累计获得学分：<strong>${totalCredits}</strong> 分</td>
      <td>平均学业绩点：<strong>${avgGpa}</strong></td>
      <td>课程平均分：<strong>${avgScore}</strong></td>
    </tr>
  </table>

  <p class="note">本成绩单由教务系统自动生成，用途：<strong>${purpose}</strong>。如需核验，请持此单至教务处加盖公章。</p>

  <div class="stamp">${universityName}</div>

  <p class="footer">打印时间：${printTime}&#160;|&#160;${universityName}教务管理系统</p>
</body>
</html>
