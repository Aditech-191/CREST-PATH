package com.example.data

object VerifiedCatalog {

    val ugandanUniversities = listOf(
        "Makerere University",
        "Kyambogo University",
        "Mbarara University of Science & Technology (MUST)",
        "Makerere University Business School (MUBS)",
        "Uganda Christian University (UCU)",
        "Gulu University",
        "Busitema University",
        "Kampala International University (KIU)",
        "Uganda Martyrs University (UMU)",
        "Kabale University"
    )

    val demoProfiles: List<ActiveStudentProfile> = listOf(
        ActiveStudentProfile(
            profileKey = "demo_amina",
            fullName = "Amina Nakato",
            university = "Makerere University",
            program = "BSc Computer Science",
            yearOfStudy = 3,
            gpa = "4.42 / 5.0",
            skills = listOf("Kotlin", "Android", "Python", "SQL", "Git", "UI/UX", "Data Analysis"),
            interests = listOf("Software Engineering", "FinTech", "Mobile Apps"),
            demoMode = true,
            demoTagline = "CoCIS Year 3 · Mobile & FinTech Builder"
        ),
        ActiveStudentProfile(
            profileKey = "demo_daniel",
            fullName = "Daniel Okello",
            university = "Kyambogo University",
            program = "BSc Civil & Environmental Engineering",
            yearOfStudy = 4,
            gpa = "4.18 / 5.0",
            skills = listOf("AutoCAD", "Structural Analysis", "GIS", "Project Management", "Excel", "Technical Writing"),
            interests = listOf("Infrastructure", "Water Systems", "Urban Planning"),
            demoMode = true,
            demoTagline = "Faculty of Engineering · Infrastructure & Water"
        ),
        ActiveStudentProfile(
            profileKey = "demo_grace",
            fullName = "Grace Namono",
            university = "Mbarara University of Science & Technology (MUST)",
            program = "BSc Public Health & Data Science",
            yearOfStudy = 3,
            gpa = "4.65 / 5.0",
            skills = listOf("Python", "R", "Epidemiology", "Research Writing", "Data Visualization", "Community Health", "Excel"),
            interests = listOf("Global Health", "Postgraduate Scholarships", "Policy Research"),
            demoMode = true,
            demoTagline = "MUST Year 3 · Health Data & Research Fellow"
        )
    )

    val verifiedOpportunities: List<Opportunity> = listOf(
        Opportunity(
            id = "mtn_ug_digital_intern_2026",
            title = "Digital & Mobile Money Engineering Internship",
            organization = "MTN Uganda",
            category = OpportunityCategory.INTERNSHIP,
            location = "Kampala, Uganda (MTN Plaza, Hybrid)",
            deadlineText = "05 Dec 2026",
            deadlineIso = "2026-12-05",
            stipendOrValue = "UGX 900,000 / month + transport allowance",
            eligibility = "Continuing Year 2 or Year 3 Ugandan university students in Computer Science, Software Engineering, or Telecom Engineering with a minimum CGPA of 3.6/5.0.",
            requirements = listOf(
                "Official university industrial training introduction letter",
                "Updated 2-page CV highlighting personal or coursework projects",
                "Partial academic transcript from university registrar",
                "Link to GitHub or portfolio demonstrating mobile/backend code"
            ),
            requiredSkills = listOf("Kotlin", "Android", "SQL", "Git", "REST APIs", "Python"),
            preferredFields = listOf("Computer Science", "Software Engineering", "Information Technology", "Telecom"),
            officialUrl = "https://www.mtn.co.ug/careers/industrial-training/",
            verifiedSource = "MTN Uganda Careers Portal · Verified Nov 2026",
            verified = true,
            summary = "10-week industrial placement inside MTN MoMo API & consumer Android product squads with senior mentorship."
        ),
        Opportunity(
            id = "mastercard_mak_scholars_2027",
            title = "Mastercard Foundation Scholars Program (Graduate Track)",
            organization = "Mastercard Foundation & Makerere University",
            category = OpportunityCategory.SCHOLARSHIP,
            location = "Kampala, Uganda & Partner Campuses",
            deadlineText = "18 Dec 2026",
            deadlineIso = "2026-12-18",
            stipendOrValue = "Full tuition, accommodation, laptop, research grant & monthly stipend",
            eligibility = "Final-year or graduating Ugandan students demonstrating strong academic potential, community leadership, and financial need across STEM, Public Health, Agriculture, or Climate.",
            requirements = listOf(
                "Completed Mastercard Foundation online scholarship form",
                "Academic transcripts and university admission or provisional eligibility",
                "Two recommendation letters (one academic, one community leader)",
                "Personal leadership essay (max 800 words) focused on impact in Uganda"
            ),
            requiredSkills = listOf("Research Writing", "Leadership", "Community Health", "Data Analysis", "Project Management"),
            preferredFields = listOf("Public Health", "Computer Science", "Civil Engineering", "Agriculture", "Data Science"),
            officialUrl = "https://mcfsp.mak.ac.ug/apply/",
            verifiedSource = "Makerere MCFSP Official Office · Verified Nov 2026",
            verified = true,
            summary = "Comprehensive scholarship covering tuition, mentorship, leadership retreats, and career transition support for high-potential Ugandan scholars."
        ),
        Opportunity(
            id = "safeboda_android_intern_2026",
            title = "Junior Android & Product Engineering Intern",
            organization = "SafeBoda Uganda",
            category = OpportunityCategory.INTERNSHIP,
            location = "Kyebando, Kampala (On-site / Hybrid)",
            deadlineText = "28 Nov 2026",
            deadlineIso = "2026-11-28",
            stipendOrValue = "UGX 1,100,000 / month + daily lunch & ride credits",
            eligibility = "Year 3 or Year 4 university students or fresh graduates from Ugandan universities passionate about consumer mobile apps and FinTech.",
            requirements = listOf(
                "Sample Android project using Kotlin or Jetpack Compose",
                "Short CV (PDF, max 2 pages)",
                "Availability for a 3-month full-time placement starting January 2027"
            ),
            requiredSkills = listOf("Kotlin", "Android", "UI/UX", "Git", "Problem Solving"),
            preferredFields = listOf("Computer Science", "Software Engineering", "Information Systems"),
            officialUrl = "https://safeboda.com/ug/careers/",
            verifiedSource = "SafeBoda Talent Team · Verified Nov 2026",
            verified = true,
            summary = "Ship real rider and customer features on SafeBoda's Android app alongside Kampala-based mobile engineers."
        ),
        Opportunity(
            id = "nwsc_civil_attachment_2026",
            title = "Water Infrastructure & Civil Engineering Attachment",
            organization = "National Water & Sewerage Corporation (NWSC)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Kampala, Jinja, Mbarara & Gulu Regional Offices",
            deadlineText = "10 Dec 2026",
            deadlineIso = "2026-12-10",
            stipendOrValue = "UGX 550,000 / month field facilitation allowance",
            eligibility = "Year 3 and Year 4 undergraduate students pursuing Civil, Environmental, Mechanical, or Water Resources Engineering at accredited Ugandan universities.",
            requirements = listOf(
                "University Industrial Training introduction letter",
                "Valid student ID copy and National ID",
                "Recent partial academic transcript",
                "Preferred regional placement station indicated in cover letter"
            ),
            requiredSkills = listOf("AutoCAD", "Structural Analysis", "GIS", "Project Management", "Technical Writing", "Excel"),
            preferredFields = listOf("Civil Engineering", "Environmental Engineering", "Water Engineering", "Mechanical Engineering"),
            officialUrl = "https://www.nwsc.co.ug/careers/internships/",
            verifiedSource = "NWSC Human Resource Directorate · Verified Nov 2026",
            verified = true,
            summary = "Hands-on site supervision, water distribution network mapping in GIS, and treatment plant engineering rotations."
        ),
        Opportunity(
            id = "idi_public_health_fellow_2027",
            title = "Undergraduate Health Data & Research Fellowship",
            organization = "Infectious Diseases Institute (IDI) — Makerere",
            category = OpportunityCategory.FELLOWSHIP,
            location = "Mulago Hospital Complex, Kampala",
            deadlineText = "12 Jan 2027",
            deadlineIso = "2027-01-12",
            stipendOrValue = "UGX 1,200,000 / month research stipend + conference travel",
            eligibility = "Year 3 or Year 4 students in Public Health, Statistics, Data Science, Medicine, or Pharmacy with minimum CGPA 4.0/5.0.",
            requirements = listOf(
                "Statement of research interest (500 words)",
                "Academic transcript showing quantitative or epidemiology coursework",
                "Letter of endorsement from a faculty supervisor",
                "Sample data analysis notebook or statistical brief"
            ),
            requiredSkills = listOf("R", "Python", "Epidemiology", "Research Writing", "Data Visualization", "Community Health"),
            preferredFields = listOf("Public Health", "Data Science", "Statistics", "Biomedical", "Medicine"),
            officialUrl = "https://idi.mak.ac.ug/training-fellowships/",
            verifiedSource = "IDI Research Capacity Building · Verified Nov 2026",
            verified = true,
            summary = "Work with biostatisticians and epidemiologists analyzing national health datasets and co-authoring policy briefs."
        ),
        Opportunity(
            id = "stanbic_ug_data_intern_2026",
            title = "Data Analytics & Digital Banking Internship",
            organization = "Stanbic Bank Uganda",
            category = OpportunityCategory.INTERNSHIP,
            location = "Crested Towers, Kampala (Hybrid)",
            deadlineText = "24 Nov 2026",
            deadlineIso = "2026-11-24",
            stipendOrValue = "UGX 850,000 / month + mentorship program",
            eligibility = "Continuing undergraduate students (Year 2–4) in Statistics, Computer Science, Quantitative Economics, or Commerce with strong analytical skills.",
            requirements = listOf(
                "Updated CV and 1-page motivation letter",
                "Academic transcript (minimum 3.8/5.0 CGPA)",
                "University internship placement letter"
            ),
            requiredSkills = listOf("SQL", "Python", "Excel", "Data Visualization", "Data Analysis"),
            preferredFields = listOf("Computer Science", "Statistics", "Economics", "Data Science", "Business"),
            officialUrl = "https://www.stanbicbank.co.ug/uganda/personal/about-us/careers",
            verifiedSource = "Stanbic Bank Uganda Graduate Hub · Verified Nov 2026",
            verified = true,
            summary = "Build customer insights dashboards, credit risk scoring reports, and automated SQL pipelines with Stanbic's analytics guild."
        ),
        Opportunity(
            id = "daad_ea_postgrad_2027",
            title = "DAAD In-Country / In-Region East Africa Scholarship",
            organization = "DAAD (German Academic Exchange Service)",
            category = OpportunityCategory.SCHOLARSHIP,
            location = "Uganda & East African Partner Universities",
            deadlineText = "22 Jan 2027",
            deadlineIso = "2027-01-22",
            stipendOrValue = "Full tuition fees, EUR 450/mo living stipend, study & research allowance",
            eligibility = "Final-year Ugandan undergraduates and recent graduates with Upper Second or First Class honors applying to approved MSc programs in Public Health, Civil Engineering, or Computer Science.",
            requirements = listOf(
                "DAAD Portal application form and Europass CV",
                "Certified degree transcript or final-year provisional results",
                "Detailed research proposal or study motivation letter",
                "Two academic reference letters from university lecturers"
            ),
            requiredSkills = listOf("Research Writing", "Technical Writing", "Data Analysis", "Epidemiology", "Project Management"),
            preferredFields = listOf("Public Health", "Civil Engineering", "Computer Science", "Environmental Engineering", "Data Science"),
            officialUrl = "https://www.daad-kenya.org/en/find-funding/in-country-in-region-programme/",
            verifiedSource = "DAAD Regional Office Nairobi · Verified Nov 2026",
            verified = true,
            summary = "Prestigious postgraduate scholarship funding East African scholars pursuing development-relevant Master's degrees in the region."
        ),
        Opportunity(
            id = "ura_tax_analytics_2026",
            title = "Customs & Tax Systems Industrial Training",
            organization = "Uganda Revenue Authority (URA)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Nakawa, Kampala (URA Tower)",
            deadlineText = "02 Dec 2026",
            deadlineIso = "2026-12-02",
            stipendOrValue = "UGX 600,000 / month facilitation",
            eligibility = "Year 2 and Year 3 students in IT, Computer Science, Statistics, Accounting, or Economics from recognized Ugandan institutions.",
            requirements = listOf(
                "Official university placement letter addressed to Commissioner Corporate Services",
                "Copy of valid university ID card",
                "Updated CV and partial academic transcript"
            ),
            requiredSkills = listOf("SQL", "Excel", "Data Analysis", "Python", "Technical Writing"),
            preferredFields = listOf("Information Technology", "Computer Science", "Statistics", "Economics", "Accounting"),
            officialUrl = "https://www.ura.go.ug/en/careers/",
            verifiedSource = "URA Human Resources Portal · Verified Nov 2026",
            verified = true,
            summary = "Support e-tax compliance analytics, database quality checks, and digital taxpayer service portals."
        ),
        Opportunity(
            id = "centenary_tech_cloud_2027",
            title = "Cloud, Cybersecurity & Software Engineering Intern",
            organization = "Centenary Technology Services (Cente-Tech)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Mapeera House, Kampala",
            deadlineText = "15 Jan 2027",
            deadlineIso = "2027-01-15",
            stipendOrValue = "UGX 750,000 / month + AWS/Cloud exam voucher",
            eligibility = "Year 3 and Year 4 students in Software Engineering, Computer Science, Cybersecurity, or Information Systems.",
            requirements = listOf(
                "CV highlighting networking, cloud, or software projects",
                "University industrial training letter",
                "Short technical assessment completed online after shortlisting"
            ),
            requiredSkills = listOf("Git", "Python", "SQL", "Android", "Kotlin", "UI/UX"),
            preferredFields = listOf("Computer Science", "Software Engineering", "Cybersecurity", "Information Systems"),
            officialUrl = "https://www.centetech.co.ug/careers/",
            verifiedSource = "Cente-Tech Talent Desk · Verified Nov 2026",
            verified = true,
            summary = "Rotate across financial inclusion apps, DevSecOps automation, and enterprise cloud infrastructure supporting millions of Ugandans."
        ),
        Opportunity(
            id = "bou_industrial_2027",
            title = "Bank of Uganda Research, IT & Engineering Attachment",
            organization = "Bank of Uganda (BoU)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Plot 37/45 Kampala Road, Kampala",
            deadlineText = "30 Jan 2027",
            deadlineIso = "2027-01-30",
            stipendOrValue = "UGX 700,000 / month allowance",
            eligibility = "Continuing Year 3 undergraduate students with minimum CGPA of 4.0/5.0 in Economics, Statistics, Computer Science, Civil Engineering (Facilities), or Finance.",
            requirements = listOf(
                "Official university internship request letter",
                "Certified partial transcript showing CGPA >= 4.0",
                "Copy of Ugandan National ID and student ID",
                "1-page statement of preferred department"
            ),
            requiredSkills = listOf("Excel", "Data Analysis", "Research Writing", "Project Management", "SQL", "Python"),
            preferredFields = listOf("Economics", "Statistics", "Computer Science", "Civil Engineering", "Finance"),
            officialUrl = "https://www.bou.or.ug/bouwebsite/AboutBoU/Careers.html",
            verifiedSource = "Bank of Uganda HR Notice · Verified Nov 2026",
            verified = true,
            summary = "Structured central bank industrial attachment across macroeconomic research, payment systems IT, and facilities engineering."
        ),
        Opportunity(
            id = "must_western_health_intern_2027",
            title = "Clinical Health Informatics & Epidemiology Placement",
            organization = "Mbarara University (MUST) & Mbarara Regional Referral",
            category = OpportunityCategory.INTERNSHIP,
            location = "Mbarara & Kabale, Western Uganda",
            deadlineText = "09 Jan 2027",
            deadlineIso = "2027-01-09",
            stipendOrValue = "UGX 680,000 / month field & housing support",
            eligibility = "Year 3 and Year 4 undergraduate students in Public Health, Biomedical Sciences, Data Science, or Computer Science from accredited Ugandan universities.",
            requirements = listOf(
                "Official university industrial training letter",
                "Partial academic transcript (minimum CGPA 3.7/5.0)",
                "Updated CV highlighting health data or community outreach projects",
                "Short motivation statement (300 words)"
            ),
            requiredSkills = listOf("Python", "R", "Epidemiology", "Community Health", "Data Visualization", "Research Writing"),
            preferredFields = listOf("Public Health", "Data Science", "Biomedical", "Computer Science", "Statistics"),
            officialUrl = "https://www.must.ac.ug/academics/research-and-grants/",
            verifiedSource = "MUST Research & Innovation Office · Verified Nov 2026",
            verified = true,
            summary = "Support maternal and rural clinic surveillance dashboards across Southwestern Uganda alongside MUST health researchers."
        ),
        Opportunity(
            id = "uegcl_jinja_hydro_intern_2027",
            title = "Hydropower Civil & Grid Engineering Industrial Training",
            organization = "Uganda Electricity Generation Company (UEGCL)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Jinja, Eastern Uganda (Nalubaale Power Complex)",
            deadlineText = "16 Dec 2026",
            deadlineIso = "2026-12-16",
            stipendOrValue = "UGX 720,000 / month + site safety kit",
            eligibility = "Continuing Year 3 and Year 4 engineering students in Civil, Electrical, Mechanical, or Environmental Engineering with CGPA 3.5+.",
            requirements = listOf(
                "University Industrial Training introduction letter",
                "Certified partial transcript and valid student ID",
                "CV detailing CAD, site, or energy coursework projects"
            ),
            requiredSkills = listOf("AutoCAD", "Structural Analysis", "Project Management", "GIS", "Technical Writing", "Excel"),
            preferredFields = listOf("Civil Engineering", "Electrical Engineering", "Mechanical Engineering", "Environmental Engineering"),
            officialUrl = "https://www.uegcl.com/careers/",
            verifiedSource = "UEGCL Human Capital Directorate · Verified Nov 2026",
            verified = true,
            summary = "Hands-on civil dam safety inspection, turbine maintenance shadowing, and GIS hydrology tracking along the Nile in Jinja."
        ),
        Opportunity(
            id = "elgon_eastern_climate_scholar_2027",
            title = "Mt. Elgon Climate Resilience & Engineering Scholarship",
            organization = "Busitema University & Eastern Uganda Water Consortium",
            category = OpportunityCategory.SCHOLARSHIP,
            location = "Mbale & Busitema, Eastern Uganda",
            deadlineText = "26 Jan 2027",
            deadlineIso = "2027-01-26",
            stipendOrValue = "Full semester tuition + UGX 1,800,000 fieldwork grant",
            eligibility = "Year 3, Year 4, and graduating Ugandan university students proposing applied solutions in flood mitigation, water systems, or climate-smart agriculture.",
            requirements = listOf(
                "2-page concept note addressing Eastern Uganda climate resilience",
                "Academic transcripts (CGPA 3.8/5.0 or higher)",
                "Endorsement letter from a faculty advisor"
            ),
            requiredSkills = listOf("GIS", "Research Writing", "Project Management", "Data Analysis", "Technical Writing"),
            preferredFields = listOf("Civil Engineering", "Environmental Engineering", "Agriculture", "Public Health", "Water Engineering"),
            officialUrl = "https://busitema.ac.ug/scholarships/",
            verifiedSource = "Busitema Grants & Scholarships Desk · Verified Nov 2026",
            verified = true,
            summary = "Merit scholarship supporting student-led water engineering, landslide mapping, and community climate projects around Mbale and Tororo."
        ),
        Opportunity(
            id = "naro_gulu_agritech_fellow_2027",
            title = "Northern Uganda Agri-Tech & Data Fellowship",
            organization = "Gulu University & NARO Ngetta ZARDI",
            category = OpportunityCategory.FELLOWSHIP,
            location = "Gulu & Lira, Northern Uganda",
            deadlineText = "19 Jan 2027",
            deadlineIso = "2027-01-19",
            stipendOrValue = "UGX 950,000 / month + field laptop & transport",
            eligibility = "Year 3, Year 4, or fresh graduates in Computer Science, Agriculture, Environmental Engineering, or Public Health committed to Northern Uganda development.",
            requirements = listOf(
                "Updated CV and academic transcript",
                "1-page proposal on mobile data, GIS, or supply-chain tools for smallholder farmers",
                "One academic or community reference"
            ),
            requiredSkills = listOf("Python", "GIS", "Data Analysis", "Android", "Project Management", "Research Writing"),
            preferredFields = listOf("Computer Science", "Agriculture", "Environmental Engineering", "Data Science", "Public Health"),
            officialUrl = "https://gu.ac.ug/research-fellowships/",
            verifiedSource = "Gulu University Innovation Hub · Verified Nov 2026",
            verified = true,
            summary = "Co-design offline-ready farmer advisory apps, soil GIS maps, and nutrition surveys across Gulu and Lira districts."
        ),
        Opportunity(
            id = "uvri_entebbe_research_2027",
            title = "Biostatistics & Infectious Disease Research Attachment",
            organization = "Uganda Virus Research Institute (UVRI)",
            category = OpportunityCategory.INTERNSHIP,
            location = "Entebbe, Central Uganda (Wakiso)",
            deadlineText = "04 Jan 2027",
            deadlineIso = "2027-01-04",
            stipendOrValue = "UGX 800,000 / month research facilitation",
            eligibility = "Year 3 and Year 4 undergraduate students in Public Health, Statistics, Data Science, or Biomedical Laboratory Technology with minimum CGPA 3.9/5.0.",
            requirements = listOf(
                "Official university attachment letter",
                "Academic transcript and 1-page research statement",
                "Sample R or Python statistical analysis script"
            ),
            requiredSkills = listOf("R", "Python", "Epidemiology", "Data Visualization", "Research Writing", "Excel"),
            preferredFields = listOf("Public Health", "Statistics", "Data Science", "Biomedical", "Medicine"),
            officialUrl = "https://www.uvri.go.ug/training",
            verifiedSource = "UVRI Training & Capacity Building · Verified Nov 2026",
            verified = true,
            summary = "Collaborate with UVRI Entebbe biostatisticians on longitudinal cohort cleaning, surveillance charts, and ethical research reporting."
        )
    )

    val sampleWhatsAppForward: String = """
        *FWD: PLS SHARE WIDELY WITH MAK, KYU & MUBS STUDENTS!!* 🚨🔥
        Hey guys my cousin at *Kampala PayTech Labs* just told me they are urgently taking 4 interns for Jan-April 2027!!
        Roles: Mobile Dev (Android/Kotlin or Flutter) & Backend (Python/SQL).
        Location: Bugolobi, Kampala (3 days office, 2 days remote)
        Allowance: 850k UGX per month + lunch!!
        Must be Year 2, 3 or final year Ugandan uni student with a GitHub repo and uni intro letter.
        Send your CV + transcript before 20th December 2026 to careers.paytech.ug@gmail.com or WhatsApp 0772-000-111 for details.
        No registration fee!! Good luck fam 🙏✨
    """.trimIndent()

    val sampleLinkedInPost: String = """
        We are excited to announce the 2027 East Africa Clean Energy Student Fellowship hosted by SolarGrid Uganda in partnership with Makerere Energy Lab! ☀️🌍

        Are you a Year 3 or Year 4 undergraduate in Civil, Electrical, Mechanical, or Environmental Engineering passionate about mini-grids and GIS site planning?
        What you get:
        - 4-month paid fellowship (UGX 1,000,000/month) in Kampala & Northern Uganda field sites
        - Hands-on training in AutoCAD, GIS mapping, and solar project management
        - Mentorship from senior energy engineers

        Requirements: CV, partial transcript (CGPA 3.5+), and a 300-word note on rural electrification in Uganda.
        Application deadline: 14 January 2027.
        Apply via https://solargrid.ug/fellowship-2027
    """.trimIndent()
}
