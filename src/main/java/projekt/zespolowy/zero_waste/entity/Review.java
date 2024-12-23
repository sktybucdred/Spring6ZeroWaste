package projekt.zespolowy.zero_waste.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;


    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "rating", nullable = false)
    private int rating; // Assuming a scale from 1 to

    @Column(name = "target_user_id")
    private Long targetUserId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}