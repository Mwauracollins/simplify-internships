package org.simplifyinternships.simplifyinternships.entities.jobentities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.simplifyinternships.simplifyinternships.entities.Company;

import java.util.Date;

/*
This entity defines the job. It incorporates all jobs like
attachments and internships.
 */
@Data
@Builder
@Entity
@Table(name = "job_opportunity")
@NoArgsConstructor
@AllArgsConstructor
public class JobOpportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Integer jobId;
    @Column(name = "name")
    private String name;
    @Column(name = "date_posted")
    private Date datePosted;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    @JoinColumn(name = "position_id")
    @ManyToOne
    private Position position;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private JobCategory category;
    @Column(name = "job_type")
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    @OneToOne(mappedBy = "jobOpportunity")
    private Attachment attachment;
    @OneToOne(mappedBy = "jobOpportunity")
    private Internship internship;



}
