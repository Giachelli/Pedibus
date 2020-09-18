package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllRoutesVM {
    List<RouteVM> lines;
}
