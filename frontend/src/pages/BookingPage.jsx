import React, { useState } from 'react';

const BookingPage = () => {
    const [selectedSlot, setSelectedSlot] = useState('');
    const slots = ['10:00 AM - 11:00 AM', '02:00 PM - 03:00 PM', '05:00 PM - 06:00 PM'];

    const [myBookings, setMyBookings] = useState([
        { id: 101, providerName: 'Arafat Hossain', service: 'Electrical Repair', date: '2026-10-20', slot: '10:00 AM', status: 'Confirmed' }
    ]);

    const handleCancel = (id) => {
        setMyBookings(myBookings.filter(b => b.id !== id));
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <h2>Booking & Slot Management</h2>
            <br />
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
                <h3>Select Available Slot</h3>
                <div style={{ display: 'flex', gap: '10px', margin: '15px 0' }}>
                    {slots.map((slot, index) => (
                        <button
                            key={index}
                            onClick={() => setSelectedSlot(slot)}
                            style={{
                                padding: '10px 15px',
                                border: selectedSlot === slot ? '2px solid #007bff' : '1px solid #ccc',
                                background: selectedSlot === slot ? '#e7f3ff' : '#fff',
                                borderRadius: '4px', cursor: 'pointer'
                            }}
                        >
                            {slot}
                        </button>
                    ))}
                </div>
                <button style={{ padding: '10px 20px', background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    Confirm Booking
                </button>
            </div>

            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                <h3>My Bookings</h3>
                {myBookings.map(b => (
                    <div key={b.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #eee' }}>
                        <div>
                            <b>{b.providerName}</b> - {b.service}
                            <p style={{ color: '#666', fontSize: '14px', margin: '3px 0' }}>Date: {b.date} | Slot: {b.slot}</p>
                            <span style={{ color: 'green', fontSize: '12px', fontWeight: 'bold' }}>● {b.status}</span>
                        </div>
                        <div>
                            <button style={{ padding: '6px 12px', background: '#ffc107', border: 'none', borderRadius: '4px', marginRight: '5px', cursor: 'pointer' }}>Reschedule</button>
                            <button onClick={() => handleCancel(b.id)} style={{ padding: '6px 12px', background: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Cancel</button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default BookingPage;