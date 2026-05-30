package com.hci.scholarship.model;

public class ScholarshipApplication {
    private int id;
    private String studentName;
    private String indexNumber;
    private String faculty;
    private String studyProgram;
    private int studyYear;
    private double averageGrade;
    private double familyIncome;
    private String scholarshipType;
    private String email;
    private String phone;
    private String municipality;
    private int ectsCredits;
    private int householdMembers;
    private String specialCategory;
    private String scholarshipCycle;
    private String motivation;
    private String documentSummary;
    private String status;
    private String gender;
    private boolean documentsConfirmed;
    private String aiRecommendation;
    private double aiScore;
    private String createdAt;

    public ScholarshipApplication() {}

    public ScholarshipApplication(int id, String studentName, String indexNumber, String faculty, String studyProgram,
                                  int studyYear, double averageGrade, double familyIncome, String scholarshipType,
                                  String email, String phone, String municipality, int ectsCredits, int householdMembers,
                                  String specialCategory, String scholarshipCycle, String motivation, String documentSummary,
                                  String status, String gender, boolean documentsConfirmed, String aiRecommendation,
                                  double aiScore, String createdAt) {
        this.id = id;
        this.studentName = studentName;
        this.indexNumber = indexNumber;
        this.faculty = faculty;
        this.studyProgram = studyProgram;
        this.studyYear = studyYear;
        this.averageGrade = averageGrade;
        this.familyIncome = familyIncome;
        this.scholarshipType = scholarshipType;
        this.email = email;
        this.phone = phone;
        this.municipality = municipality;
        this.ectsCredits = ectsCredits;
        this.householdMembers = householdMembers;
        this.specialCategory = specialCategory;
        this.scholarshipCycle = scholarshipCycle;
        this.motivation = motivation;
        this.documentSummary = documentSummary;
        this.status = status;
        this.gender = gender;
        this.documentsConfirmed = documentsConfirmed;
        this.aiRecommendation = aiRecommendation;
        this.aiScore = aiScore;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getIndexNumber() { return indexNumber; }
    public void setIndexNumber(String indexNumber) { this.indexNumber = indexNumber; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getStudyProgram() { return studyProgram; }
    public void setStudyProgram(String studyProgram) { this.studyProgram = studyProgram; }
    public int getStudyYear() { return studyYear; }
    public void setStudyYear(int studyYear) { this.studyYear = studyYear; }
    public double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(double averageGrade) { this.averageGrade = averageGrade; }
    public double getFamilyIncome() { return familyIncome; }
    public void setFamilyIncome(double familyIncome) { this.familyIncome = familyIncome; }
    public String getScholarshipType() { return scholarshipType; }
    public void setScholarshipType(String scholarshipType) { this.scholarshipType = scholarshipType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public int getEctsCredits() { return ectsCredits; }
    public void setEctsCredits(int ectsCredits) { this.ectsCredits = ectsCredits; }
    public int getHouseholdMembers() { return householdMembers; }
    public void setHouseholdMembers(int householdMembers) { this.householdMembers = householdMembers; }
    public String getSpecialCategory() { return specialCategory; }
    public void setSpecialCategory(String specialCategory) { this.specialCategory = specialCategory; }
    public String getScholarshipCycle() { return scholarshipCycle; }
    public void setScholarshipCycle(String scholarshipCycle) { this.scholarshipCycle = scholarshipCycle; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public String getDocumentSummary() { return documentSummary; }
    public void setDocumentSummary(String documentSummary) { this.documentSummary = documentSummary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public boolean isDocumentsConfirmed() { return documentsConfirmed; }
    public void setDocumentsConfirmed(boolean documentsConfirmed) { this.documentsConfirmed = documentsConfirmed; }
    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }
    public double getAiScore() { return aiScore; }
    public void setAiScore(double aiScore) { this.aiScore = aiScore; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

