<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
  <meta charset="UTF-8"/>
  <style>
    body { font-family: "ChineseFont", "SimSun", "SimHei", serif; font-size: 12px; color: #1a1a1a; margin: 0; padding: 30px 40px; }
    h1.title { font-size: 20px; font-weight: bold; text-align: center; letter-spacing: 2px; margin: 0 0 2px 0; }
    h2.title2 { font-size: 14px; font-weight: bold; text-align: center; letter-spacing: 1px; margin: 0 0 10px 0; }
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
    div.stamp { width: 88px; height: 88px; border: 2px solid #b91c1c; border-radius: 50%;
                color: #b91c1c; font-size: 10px; text-align: center; line-height: 1.4;
                padding-top: 28px; box-sizing: border-box;
                -fs-transform: rotate(-15deg); margin-left: auto; }
  </style>
</head>
<body>
  <h1 class="title">${universityNameEn}</h1>
  <h2 class="title2">OFFICIAL ACADEMIC TRANSCRIPT</h2>
  <hr class="divider"/>

  <table class="info-table">
    <tr>
      <td><span>Name: </span><strong>${studentName}</strong></td>
      <td><span>Student ID: </span>${studentNo}</td>
      <td><span>Gender: </span>${genderEn}</td>
    </tr>
    <tr>
      <td><span>Year: </span>${grade!'-'}</td>
      <td><span>Major: </span>${major!'-'}</td>
      <td><span>Class: </span>${className!'-'}</td>
    </tr>
  </table>

  <table>
    <thead>
      <tr>
        <th>#</th>
        <th>Term</th>
        <th>Course Code</th>
        <th>Course Name</th>
        <th>Credits</th>
        <th>Final Exam</th>
        <th>Overall Score</th>
        <th>Grade</th>
        <th>GPA</th>
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
      <td>Total Courses: <strong>${totalCourses}</strong></td>
      <td>Total Credits: <strong>${totalCredits}</strong></td>
      <td>Cumulative GPA: <strong>${avgGpa}</strong></td>
      <td>Average Score: <strong>${avgScore}</strong></td>
    </tr>
  </table>

  <p class="note">This transcript is system-generated for: <strong>${purpose}</strong>. For official use, please have it stamped by the Academic Affairs Office.</p>

  <div class="stamp">${universityNameEn}</div>

  <p class="footer">Generated at: ${printTime}&#160;|&#160;${universityNameEn} Academic System</p>
</body>
</html>
