package jnode.main.threads;

import java.util.ArrayList;
import java.util.List;

import jnode.dto.Link;
import jnode.ftn.tosser.FtnTosser;
import jnode.logger.Logger;
import jnode.protocol.io.Message;

public enum TosserQueue {
	INSTANSE;
	private static class TossRequest {
		private Message message;
		private Link link;

		public TossRequest(Message message, Link link) {
			super();
			this.message = message;
			this.link = link;
		}
	}

	private final static Logger logger = Logger.getLogger(TosserQueue.class);
	private List<TossRequest> requests;

	private TosserQueue() {
		requests = new ArrayList<TossRequest>();
	}

	public void toss() {
		if (requests.size() > 0) {
			logger.l5("В TosserQueue " + requests.size() + " файлов, запускаем тоссер");
			ArrayList<TossRequest> currentQueue = new ArrayList<TossRequest>(
					requests);
			requests = new ArrayList<TossRequest>();
			logger.l5("Запускаем тоссер");
			FtnTosser tosser = new FtnTosser();
			for (TossRequest request : currentQueue) {
				tosser.tossIncoming(request.message, request.link);
			}
			tosser.tossInbound();
			tosser.end();
		}
	}

	public synchronized void add(Message message, Link link) {
		logger.l5("Добавлено сообщение " + message.getMessageName()
				+ " в очередь тоссера");
		requests.add(new TossRequest(message, link));
	}

}
