package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Evaluation;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.EvaluationRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EvaluationService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final InterviewRepository interviewRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    public EvaluationService(InterviewRepository interviewRepository,
                             EvaluationRepository evaluationRepository,
                             UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Interview> findMyInterviews(Long interviewerId) {
        return interviewRepository.findByInterviewer_IdOrderByScheduledAtDesc(interviewerId);
    }

    @Transactional(readOnly = true)
    public Interview findMyInterviewOrThrow(Long interviewId, SessionUser sessionUser) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch phỏng vấn id=" + interviewId));
        ensureIsAssignedInterviewer(interview, sessionUser);
        return interview;
    }

    @Transactional(readOnly = true)
    public Evaluation findExistingEvaluation(Long interviewId) {
        return evaluationRepository.findByInterview_Id(interviewId).orElse(null);
    }

    // SCR-19: trả về Interview để controller lấy applicationId, điều hướng về SCR-17 sau khi nộp
    @Transactional
    public Interview submitEvaluation(Long interviewId, SessionUser sessionUser, Integer rating, String feedback) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch phỏng vấn id=" + interviewId));
        ensureIsAssignedInterviewer(interview, sessionUser);

        if (evaluationRepository.existsByInterview_Id(interviewId)) {
            throw new IllegalArgumentException("Lịch phỏng vấn này đã được đánh giá, không thể nộp lại lần nữa");
        }
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ " + MIN_RATING + " đến " + MAX_RATING);
        }
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập nhận xét chi tiết");
        }

        User interviewer = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        Evaluation evaluation = new Evaluation(interview, interviewer, rating, feedback.trim());
        evaluationRepository.save(evaluation);
        return interview;
    }

    private void ensureIsAssignedInterviewer(Interview interview, SessionUser sessionUser) {
        if (interview.getInterviewer() == null
                || sessionUser == null
                || !interview.getInterviewer().getId().equals(sessionUser.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
    }
}
