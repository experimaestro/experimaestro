#pragma once

#include <thread>
#include <chrono>
#include <atomic>

#include "base_sink.h"
#include "../logger.h"
#include "../details/blocking_queue.h"
#include "../details/log_msg.h"


using namespace std;
namespace c11log
{
namespace sinks
{

class async_sink : public base_sink
{
public:

    using queue_t = c11log::details::blocking_queue<std::shared_ptr<details::log_msg>>;
    using size_type = queue_t::size_type;

    explicit async_sink(const size_type max_queue_size);
    ~async_sink();
    void add_sink(logger::sink_ptr sink);
    void remove_sink(logger::sink_ptr sink_ptr);

    //Wait to remaining items (if any) in the queue to be written and shutdown
    void shutdown(const std::chrono::seconds& timeout);


protected:
    void _sink_it(const details::log_msg& msg) override;
    void _thread_loop();

private:
    c11log::logger::sinks_vector_t _sinks;
    std::atomic<bool> _active;
    queue_t _q;
    std::thread _back_thread;
    //Clear all remaining messages(if any), stop the _back_thread and join it
    void _shutdown();
};
}
}

///////////////////////////////////////////////////////////////////////////////
// async_sink class implementation
///////////////////////////////////////////////////////////////////////////////

inline c11log::sinks::async_sink::async_sink(const size_type max_queue_size)
    :_sinks(),
     _active(true),
     _q(max_queue_size),
     _back_thread(&async_sink::_thread_loop, this)
{}

inline c11log::sinks::async_sink::~async_sink()
{
    _shutdown();
}
inline void c11log::sinks::async_sink::_sink_it(const details::log_msg& msg)
{
    //re allocate on the heap the (stack based) message
    auto new_msg = new details::log_msg();
    *new_msg = msg;
    auto msg_size = msg.msg_buf.second;
    char *buf = new char[msg_size];
    std::memcpy(buf, msg.msg_buf.first, msg_size);
    new_msg->msg_buf = bufpair_t(buf, msg_size);
    auto new_shared = std::shared_ptr<details::log_msg>(new_msg, [](details::log_msg* msg_to_delete)
    {
        delete []msg_to_delete->msg_buf.first;
        delete msg_to_delete;
    });
    _q.push(new_shared);
}

inline void c11log::sinks::async_sink::_thread_loop()
{
    static std::chrono::seconds  pop_timeout { 1 };
    while (_active)
    {
        std::shared_ptr<details::log_msg> msg;
        if (_q.pop(msg, pop_timeout))
        {
            for (auto &sink : _sinks)
            {
                sink->log(*msg);
            }
        }
    }
}

inline void c11log::sinks::async_sink::add_sink(logger::sink_ptr sink)
{
    _sinks.push_back(sink);
}

inline void c11log::sinks::async_sink::remove_sink(logger::sink_ptr sink_ptr)
{
    _sinks.erase(std::remove(_sinks.begin(), _sinks.end(), sink_ptr), _sinks.end());
}


inline void c11log::sinks::async_sink::shutdown(const std::chrono::seconds &timeout)
{
    auto until = std::chrono::system_clock::now() + timeout;
    while (_q.size() > 0 && std::chrono::system_clock::now() < until)
    {
        std::this_thread::sleep_for(std::chrono::milliseconds(200));
    }
    _shutdown();
}

inline void c11log::sinks::async_sink::_shutdown()
{
    if(_active)
    {
        _active = false;
        if (_back_thread.joinable())
            _back_thread.join();
    }
}
