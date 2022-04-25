package com.reactive.toby.live;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScedulerEx {
	//Reactive Streams
	//기초적이고 핵심적인 메서드에대한 이해가 있어야됨
	//표준에 나와있는 protocol => publisher <-> subscriber
	
	//스케줄러 방식을 지정하자(publishOn subscribeOn)
	public static void main(String[] args) {
		Publisher<Integer> pub = (Subscriber<? super Integer> sub) -> {
			sub.onSubscribe(new Subscription() {
				@Override
				public void request(long n) {
					log.debug("Requese()");
					sub.onNext(1);
					sub.onNext(2);
					sub.onNext(3);
					sub.onNext(4);
					sub.onNext(5);
					sub.onComplete();
				}
	
				@Override
				public void cancel() {
					
				}
			});
		};
		//pub
		
		//Consumer가 처리가 빠르고, DataLoading이 느린 경우 PubOnSub사용 하면 됨
		/* Publisher<Integer> subOnPub = sub-> {
			
			//코어 스레드 1 맥시멈 스레드 1개인 스레드 생성
			//앞의 작업을 마치기 전 까지 큐에 담아둠
			//스레드이름을 만들기 위해선 CustomizableThreadFactory의 스레드 네임을 재 지정해주면 됨
			ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
				@Override
				public String getThreadNamePrefix() {
					return "subOn - ";
				}
			});
			es.execute(()->pub.subscribe(sub));
		}; */
		
		// subscriber가 받는 쪽 보다 계산이 느린 경우 PublishOn 사용 하면됨
		// 데이터 생성은 빠르지만, 소모가 느린 경우
		Publisher<Integer> pubOnPub  = sub -> {
			//스레드이름을 만들기 위해선 CustomizableThreadFactory의 스레드 네임을 재 지정해주면 됨
			ExecutorService es =Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
				@Override
				public String getThreadNamePrefix() {
					return "pubOn - ";
				}
			});
			//onNext, onCompletem onError를 실제 데이터가 날라오는 부분을 별개 스레드 사용
			pub.subscribe(new Subscriber<Integer>() {
				//만약 이부분에 Thread를 선언 시 하나의 스레드에서 하나의 subscription 사용
				//ExecutorService es =Executors.newSingleThreadExecutor();
				@Override
				public void onSubscribe(Subscription s) {
					sub.onSubscribe(s);
					
				}

				@Override
				public void onNext(Integer t) {
					es.execute(()->sub.onNext(t));
					
				}

				@Override
				public void onError(Throwable t) {
					es.execute(()-> sub.onError(t));
					es.shutdown();
				}

				@Override
				public void onComplete() {
					es.execute(()-> sub.onComplete());
					es.shutdown();
				}
			});
			
		};
		
		//sub
		pubOnPub.subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription s) {
				log.debug("onSubscribe");
				s.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(Integer t) {
				log.debug("onNext : {}",t);
			}

			@Override
			public void onError(Throwable t) { 
				log.debug("onError : {}",t);
			}

			@Override
			public void onComplete() {
				log.debug("onComplete");
				
			}
		});
		System.out.println("main Thread Exit");
	}
}
