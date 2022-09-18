package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CardService {

    private CardRepository cardRepository;

    public CardService(@Autowired CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card addCard(Card card) {
        if (cardRepository.existsByNumber(card.getNumber())) throw new ResponseStatusException(HttpStatus.CONFLICT);
        return cardRepository.save(card);
    }

    public void removeCard(String card) {
        //System.out.println(card.getNumber());
        if (!cardRepository.existsByNumber(card)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        cardRepository.deleteByNumber(card);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll(Sort.by("id").ascending());
    }

    public boolean existsByNumber(String number) {
        return cardRepository.existsByNumber(number);
    }
}
