package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.LogDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;

import javax.persistence.*;

@Table(name = "logs")
@Entity()
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "total_cost")
    private Long totalCost;

    @Column(name = "product_count")
    private Long productCount;

    @ManyToOne
    private Product product;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LogType type;

    @ManyToOne()
    private Team team;

    public LogDTO toDto(){
        return new LogDTO(
                type,
                totalCost,
                productCount,
                product.getName()
        );
    }


}
