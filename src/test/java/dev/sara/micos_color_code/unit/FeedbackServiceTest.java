package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import dev.sara.micos_color_code.feedback.FeedbackRequestDTO;
import dev.sara.micos_color_code.feedback.FeedbackService;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @InjectMocks
    private FeedbackService feedbackService;

    @Mock
    private JavaMailSender mailSender;

    private FeedbackRequestDTO feedbackRequestDTO;

    @BeforeEach
    void setUp() {
        feedbackRequestDTO = new FeedbackRequestDTO(
            "concha@gmail.com",
            "Este es un mensaje de prueba del feedback"
        );
    }

    @Test
    void sendFeedback_ShouldSendEmail_WhenFeedbackIsValid() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        feedbackService.sendFeedback(feedbackRequestDTO);

        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()[0], is(equalTo("micoscolorcode@gmail.com")));
        assertThat(sentMessage.getSubject(), is(equalTo("Nuevo feedback de Micos")));
        assertThat(sentMessage.getText(), containsString("📧 De: concha@gmail.com"));
        assertThat(sentMessage.getText(), containsString("📝 Mensaje:"));
        assertThat(sentMessage.getText(), containsString("Este es un mensaje de prueba del feedback"));
    }

    @Test
    void sendFeedback_ShouldFormatEmailCorrectly_WhenCalled() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        feedbackService.sendFeedback(feedbackRequestDTO);

        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String expectedText = "📧 De: concha@gmail.com\n\n📝 Mensaje:\n" + 
                            "Este es un mensaje de prueba del feedback";
        
        assertThat(sentMessage.getText(), is(equalTo(expectedText)));
    }

    @Test
    void sendFeedback_ShouldSendToCorrectRecipient_WhenCalled() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        feedbackService.sendFeedback(feedbackRequestDTO);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo(), is(notNullValue()));
        assertThat(sentMessage.getTo().length, is(1));
        assertThat(sentMessage.getTo()[0], is(equalTo("micoscolorcode@gmail.com")));
    }
}