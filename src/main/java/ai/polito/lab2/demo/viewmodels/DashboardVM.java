package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardVM {
    int numberRoutes;
    int numberAdmin;
    int numberMules;
}
