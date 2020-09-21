package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepo roleRepo;

    /**
     * converte i ruoli da stringa ad oggetto role
     * @param roles stringa di ruoli
     * @return
     * @throws Exception
     */
    @Override
    public ArrayList<Role> convertRoles(List<String> roles) throws Exception {
        ArrayList<Role> rolesDb = new ArrayList<>();

        for (int i = 0; i < roles.size(); i++) {
            if (roleRepo.findByRole(roles.get(i)) == null) {
                throw new Exception("Errore ruolo non esistente");
            } else {
                rolesDb.add(roleRepo.findByRole(roles.get(i)));
            }
        }

        return rolesDb;
    }
}
