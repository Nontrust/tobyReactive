package com.reactive.toby;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class TobyReactiveApplication {
	@RestController
	public static class Controller{
		
		/*
		 * 스프링에서는 publisher 만 만들면된다
		 * Spring 5부터는 publisher로 리턴 가능(subscriber는 스프링이 원하는 방식으로 원하는 시점에 데이터 요청)
		 */
		@RequestMapping("/hello")
		public Publisher<String> hello (String name){
			//return -> responseBody로 리턴
			return new Publisher<String>() {
				@Override
				public void subscribe(Subscriber<? super String> s) {
					s.onSubscribe(new Subscription() {
						@Override
						public void request(long n) {
							s.onNext("Hello "+name+ "\n");
							s.onComplete();
						}
						@Override
						public void cancel() {
							
						}
					});
					
				}
			};
		}
	}

	public static void main(String[] args) {
		
	
		SpringApplication.run(TobyReactiveApplication.class, args);
		Flux.<Integer>create(e -> {
			e.next(1);
			e.next(2);
			e.next(3);
			e.complete();
		})
		.log()
		.map(s -> s*10)
		.reduce(0,(a,b)->a+b)
		.log()
		.subscribe(System.out::println);
		
	}

}
