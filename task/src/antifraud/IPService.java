package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IPService {
    private IPRepository ipRepository;

    public IPService(@Autowired IPRepository ipRepository) {
        this.ipRepository = ipRepository;
    }

    public BlacklistedIP addIP(BlacklistedIP ip) {
        if (ipRepository.existsByIp(ip.getIp()))
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        return ipRepository.save(ip);
    }

    public void deleteIp(String ip) {
        //System.out.print(ip.getIp());
        if(!ip.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!ipRepository.existsByIp(ip)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ipRepository.deleteByIp(ip);
    }

    public List<BlacklistedIP> getIps() {
        return ipRepository.findAll(Sort.by("id").ascending());
    }

    public boolean existsByIp(String ip) {
        return ipRepository.existsByIp(ip);
    }
}
